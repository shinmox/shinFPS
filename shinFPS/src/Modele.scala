import com.jme3.math.Vector3f
import com.jme3.scene.Node

/**
 * Created by shinmox on 01/04/14.
 */
class Modele {
    var GameType: String = "STR"
    val NombreCube = 400
    var Xmax = 0
    val HauteurSol: Float = 0

    // Tir
    var Shootables: Node = null
    var Marks: Marker = null

    // Entites
    private val entites: GestionnaireEntite = new GestionnaireEntite
    private var nextPopTime: Long = 5000   //millisecondes
    var MobApparitionPoint: (Int, Int, Int) = null
    var PositionCoeurDonjon: (Int, Int, Int) = null

    private var _ui: Ui = null

    def AddObserver(ui: Ui) {
        _ui = ui
    }

    def Start() {
        _ui.start()
    }

    def Init() {
        def InitXMax() {
            if (NombreCube % Math.sqrt(NombreCube) != 0)
                throw new Exception("NombreCube ne représente pas un carré")
            Xmax = Math.sqrt(NombreCube).toInt
        }
        def InitPhysics() {
            _ui.InitPhysics()
        }
        def InitKeys() {
            _ui.InitKeys()
        }
        def InitWorld() {
            def InitBlocks() {
                _ui.InitBlocks()
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
                _ui.CreateLight()
            }

            _ui.InitWorld()
            InitUndestructible()
            InitBlocks()
            CreateLight()
            Shootables = new Node("Shootables")
        }
        def InitEntites() {
            val nom = "CoeurDuDonjon"
            entites.CreateCoeurDuDonjon(_ui.getAssetManager, nom, PositionCoeurDonjon, this)
            _ui.InitEntites(entites, nom)
        }
        def InitMarks() {
            Marks = new Marker(5, _ui.getAssetManager, _ui)
        }
        def MoveCam() {
            _ui.MoveCam()
        }
        def InitPointeurs() {
            _ui.InitCurseur()
            _ui.InitCrossHair()
        }
        def InitPlayer() {
            _ui.InitPlayer()
        }

        InitXMax()

        val demiXMax: Int =
            if ((0.5f*Xmax )% 2 == 0)
                (0.5f*Xmax).toInt
            else
                (0.5f*Xmax + 1).toInt

        MobApparitionPoint = (Xmax, HauteurSol.toInt, demiXMax)
        PositionCoeurDonjon = (2, HauteurSol.toInt, demiXMax)
        nextPopTime += System.currentTimeMillis()

        InitPhysics()
        InitKeys()
        InitWorld()
        InitEntites()
        InitMarks()
        MoveCam()
        InitPointeurs()
        InitPlayer()
    }

    def TestPopMob() {
        if (CurrentTime >= nextPopTime) {
            nextPopTime += 10000

            val nom = entites.AjouterMob(_ui.getAssetManager, MobApparitionPoint, this)
            val geometry = entites.Entites(nom).Geometry
            geometry.move((MobApparitionPoint._1-1) * 2,
                            (MobApparitionPoint._2 + geometry.getLocalScale.getY) * 2,
                            (MobApparitionPoint._3-1) * 2 )
            entites.AddPhysics(nom, _ui.BulletAppState)
            Shootables.attachChild(geometry)

            println((MobApparitionPoint._3 + HauteurSol + geometry.getLocalScale.getY)*2)
            println(geometry.getLocalScale)
        }
    }
    def PlayIA() {
        entites.PlayIA()
    }

    def SwitchGamePlay() {
        if (GameType == "STR") {
            _ui.InitStr()
        }
        else {
            _ui.SwitchStr()
        }
    }
    def DonneVision(i: Int): List[Vu] = {
        throw new NotImplementedError()
        val vision: List[Vu] = null
        vision
    }

    def CurrentTime = System.currentTimeMillis
}
