import com.jme3.collision.{CollisionResult, CollisionResults}
import com.jme3.effect.{ParticleMesh, ParticleEmitter}
import com.jme3.material.Material
import com.jme3.scene.{Geometry, Node}
import com.jme3.math.{ColorRGBA, Ray, Vector3f}
import com.jme3.scene.shape.Sphere
import javax.naming.ldap.Control

/**
 * Created by shinmox on 01/04/14.
 */
class Modele {
    var GameType: String = "STR"
    val Configuration = new Configuration
    //val HauteurSol = Configuration.HauteurSol
    //val HauteurMurs = Configuration.HauteurSol
    private var _jeuEnCours = true

    // Tir
    var Shootables: Node = null
    var Marks: Marker = null

    // Entites
    val AirDeJeu = new AirDeJeu(Configuration)
    var Player: Player = null
    private val _gestionnaireEntites: GestionnaireEntite = new GestionnaireEntite(Configuration)
    private val _IA = new IA(_gestionnaireEntites.Entites, Configuration)
    private var _nextPopTime: Long = Configuration.FirstPopTime
    private var _nextIATime: Long = Configuration.IATime
    private var _ui: Ui = null

    // INITIALISATION
    def Init() {
        def InitPhysics() {
            _ui.InitPhysics()
        }
        def InitKeys() {
            _ui.InitKeys()
        }
        def InitWorld() {
            def InitBlocks() {
                _ui.InitBlocks(AirDeJeu.Table)
            }
            def InitUndestructible() {
                def InitWall() {
                    _ui.InitWall()
                }
                def InitGround() {
                    _ui.InitGround()
                }

                InitGround()
                InitWall()
            }
            def CreateLight() {
                _ui.InitLight()
            }

            Shootables = new Node("Shootables")
            _ui.InitWorld()
            InitBlocks()
            InitUndestructible()
            CreateLight()
        }
        def InitEntite() {
            val nom = Configuration.CoeurName
            _gestionnaireEntites.CreateCoeurDuDonjon(_ui.getAssetManager, nom, AirDeJeu.PositionCoeurDonjon, this)
            _ui.InitEntite(_gestionnaireEntites, nom, AirDeJeu.PositionCoeurDonjon)
        }
        def InitMarks() {
            Marks = new Marker(5, _ui.getAssetManager, _ui)
        }
        def MoveCam() {
            _ui.InitCam()
        }
        def InitInterface() {
            _ui.InitCurseur()
            _ui.InitCrossHair()
            _ui.InitInterface()
        }
        def InitPlayer() {
            val nom = Configuration.PlayerName
            val position = (_ui.PlayerPosition.getX.toInt, _ui.PlayerPosition.getY, _ui.PlayerPosition.getZ.toInt)
            Player = _gestionnaireEntites.CreatePlayer(_ui.getAssetManager, nom, position, this)
            _ui.InitPlayer()
        }
        def InitDeaths() {
            _ui.InitCoeurDeath()
            _ui.InitMinionDeath()
        }
        def InitAudio() {
            _ui.InitAudio()
        }

        _nextPopTime += System.currentTimeMillis()
        _nextIATime += System.currentTimeMillis()

        InitPhysics()
        InitKeys()
        InitWorld()
        InitEntite()
        InitMarks()
        MoveCam()
        InitInterface()
        InitPlayer()
        InitDeaths()
        InitAudio()
        
        _jeuEnCours = true
    }
    def AddObserver(ui: Ui) {
        _ui = ui
    }

    // MECANISMES DE JEU
    def SimpleUpdate() {
        if (_jeuEnCours) {
            def TestEntiteMove() {
                for (personnage <- _gestionnaireEntites.Entites.values)
                    if (personnage.Destination != null)
                        _ui.Move(personnage)
            }

            if (CurrentTime >= _nextIATime) {
                _nextIATime += Configuration.IATime

                def TestPopMob() {
                    if (CurrentTime >= _nextPopTime && _gestionnaireEntites.CompteurMob < 5) {
                        _nextPopTime += 10000

                        //TODO: Incohérence entre _walls dans l'ui et entites dans le modele ... => gestion différente de deux objets similaires
                        val point = AirDeJeu.PositionApparitionMinions
                        val nom = _gestionnaireEntites.AjouterMob(_ui.getAssetManager, point, this)
                        val geometry = _gestionnaireEntites.Entites(nom).Geometry
                        _gestionnaireEntites.AddPhysics(nom, _ui.BulletAppState)
                        Shootables.attachChild(geometry)
                    }
                }
                TestPopMob()
                _IA.SimpleUpdate()
            }
            TestEntiteMove()
        }
    }
    def GiveText(): String = {
        var text = ""
        if (_gestionnaireEntites.Entites.contains(Configuration.CoeurName)) {
            text += _gestionnaireEntites.Entites(Configuration.CoeurName).PointVie + "\n"
        }
        else
            text += "0\n"

        text += _gestionnaireEntites.CompteurMob + "\n\n"
        text += Player.Gold + "\n"
        text += Player.PointVie + "\n"
        //        text += "(" + (Player.Geometry.getWorldTranslation.getX /2.0f).toString.substring(0,3) +
        //            "," + (Player.Geometry.getWorldTranslation.getY /2.0f).toString.substring(0,3) +
        //            "," + (Player.Geometry.getWorldTranslation.getZ /2.0f).toString.substring(0,3) + ")\n"
        text
    }

    // STR - FPS
    def SwitchGamePlay() {
        if (GameType == "STR") {
            _ui.SwitchFps()
        }
        else {
            _ui.SwitchStr()
        }
    }

    // STR
    def CreateMur(x: Int, z: Int) {
        val coutMur = Configuration.CoutMur
        if (Player.Gold < coutMur) return
        Player.Gold -= coutMur

        _ui.AjouteMur(x, z)
    }
    def RemoveMur(x: Int, z: Int) {
        AirDeJeu.RemoveMur(x, z)
        if (util.Random.nextInt(10) > 6)
            Player.Gold += 1
    }

    // FPS
    def ShootAt(nom: String) {
        if (_gestionnaireEntites.Entites.contains(nom))
            _gestionnaireEntites.Entites(nom).RecoitFrappe(Player.Degats)
    }
    def KillPlayer() {
        Player.PointVie = 15
    }

    // IA
    def DonneVision(nom: String): Vision = {
        //TODO: Certainement la fonction ralentissant le plus l'IA
        val vision = new Vision(Configuration.Cote)

        def CreCaseRien(x: Int, z: Int) {
            if (vision.Cases(x)(z) == null) {
                vision.Cases(x)(z) = new Case
                vision.Cases(x)(z).Rien = true
            }
        }
        def RempliDeRien(xCible: Int, zCible: Int, xMinion: Int, zMinion: Int) {
            // On rempli entre le minion et le mur
            if (xMinion <= xCible && zMinion <= zCible)
                for (x <- xMinion until xCible ; z <- zMinion until zCible ) {
                    CreCaseRien(x, z)
                }
            else if (xMinion >= xCible && zMinion <= zCible)
                for (x <- xCible until xMinion ; z <- zMinion until zCible ) {
                    CreCaseRien(x, z)
                }
            else if (xMinion <= xCible && zMinion >= zCible)
                for (x <- xMinion until xCible ; z <- zCible until zMinion) {
                    CreCaseRien(x, z)
                }
            else if (xMinion >= xCible && zMinion >= zCible)
                for (x <- xCible until xMinion ; z <- zCible until zMinion) {
                    CreCaseRien(x, z)
                }
        }

        // Position de celui qui regarde
        val location = _gestionnaireEntites.Entites(nom).Geometry.getWorldTranslation
        val x = (_gestionnaireEntites.Entites(nom).Geometry.getWorldTranslation.getX/2.0f).toInt
        val z = (_gestionnaireEntites.Entites(nom).Geometry.getWorldTranslation.getZ/2.0f).toInt

        // Position de celui qui regarde
        val cote = Configuration.Cote
        def borne(x: Int) = if (x <= 0) 0 else if (x >= cote) cote-1 else x
        val xMinion = borne(x)
        val zMinion = borne(z)
        vision.Cases(xMinion)(zMinion)= new Case
        vision.Cases(xMinion)(zMinion).Minion = true

        for (angle <- 0 to Configuration.IaPointVue) {    //tour d'horizon par pas d'environ 3°
            val x = Math.cos(angle/20.0).toFloat
            val z = Math.sin(angle/20.0).toFloat
            val direction = new Vector3f(x, 0.0f, z)
            val results = new CollisionResults
            val ray = new Ray(location, direction)
            Shootables.collideWith(ray, results)

            if (results.size() > 1) {
                var stop = false
                for (i <- 0 until results.size() if !stop) {
                    val geometry = results.getCollision(i).getGeometry
                    val name = geometry.getName
                    if (name.substring(0, 4) == Configuration.RockStartName) {
                        val tab = name.split("_")
                        val xCible = tab(1).toInt
                        val zCible = tab(2).toInt
                        vision.Cases(xCible)(zCible)= new Case
                        vision.Cases(xCible)(zCible).Mur = true
                        RempliDeRien(xCible, zCible, xMinion, zMinion)
                        stop = true     // On ne regarde pas derrière le mur
                    }

                    else if (name == Configuration.CoeurName) {
                        val xCible = (geometry.getWorldTranslation.getX/2.0f).toInt
                        val zCible = (geometry.getWorldTranslation.getZ/2.0f).toInt
                        vision.Cases(xCible)(zCible)= new Case
                        vision.Cases(xCible)(zCible).CoeurDuDonjon = true
                        RempliDeRien(xCible, zCible, xMinion, zMinion)
                    }

                    else if (name == Configuration.PlayerName) {
                        val xCible = (geometry.getWorldTranslation.getX/2.0f).toInt
                        val zCible = (geometry.getWorldTranslation.getZ/2.0f).toInt
                        vision.Cases(xCible)(zCible)= new Case
                        vision.Cases(xCible)(zCible).Joueur = true
                        RempliDeRien(xCible, zCible, xMinion, zMinion)
                    }
                }
            }
        }
        vision
    }
    def Autour(point: (Int, Int)): Array[Array[Boolean]] = {
        _ui.GiveWallAutour(point)
    }
    def Frappe(nom: String, quantite:Int) {
        _gestionnaireEntites.Entites(nom).RecoitFrappe(quantite)
    }
    def Move(personnage: Personnage) {
        _gestionnaireEntites.MovePhysics(personnage.Nom)
    }
    def PlayMyDeath(nom: String) {
        val lieu = _gestionnaireEntites.Entites(nom).Geometry.getWorldTranslation
        if (nom == Configuration.CoeurName) {
            _ui.PlayCoeurDeath(lieu)
            _gestionnaireEntites.Kill(nom)
            _IA.CoeurDetruit()
            _jeuEnCours = false
        }
        else if (nom.substring(0, 3) == Configuration.MinionStartName){
            _ui.PlayMinionDeath(lieu)
            _gestionnaireEntites.Kill(nom)
            Player.Gold += Configuration.MinionGold
        }
        else if (nom == Configuration.PlayerName) {
            _ui.PlayMinionDeath(lieu)
            _ui.PLayPlayerDeath()
        }
    }
//    def VerifieNoWall(point: (Int, Int)): Boolean = {
//        _ui.VerifieNoWall(point)
//    }

    // HELPER
    def CurrentTime = System.currentTimeMillis
}
