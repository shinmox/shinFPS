import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.collision.shapes.{CapsuleCollisionShape, CollisionShape}
import com.jme3.bullet.control.{RigidBodyControl, CharacterControl}
import com.jme3.bullet.util.CollisionShapeFactory
import com.jme3.collision.{CollisionResult, CollisionResults}
import com.jme3.effect.{ParticleMesh, ParticleEmitter}
import com.jme3.font.BitmapText
import com.jme3.input.controls.{ActionListener, MouseButtonTrigger, KeyTrigger}
import com.jme3.input.{MouseInput, KeyInput}
import com.jme3.light.{DirectionalLight, AmbientLight}
import com.jme3.material.Material
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.{Ray, ColorRGBA, Vector3f}
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.shape.Box
import com.jme3.scene.Geometry
import com.jme3.util.TangentBinormalGenerator
import scala.collection.mutable

/**
 * Created by shinmox on 01/04/14.
 */
class Ui(_modele: Modele) extends SimpleApplication {
    def AjouteMur(x: Int, z: Int) {
        //TODO: Code proche d'InitBlocks
        val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"))
        mat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"))
        mat.setBoolean("UseMaterialColors", true)
        mat.setColor("Specular", ColorRGBA.White)
        mat.setColor("Diffuse", ColorRGBA.Gray)
        mat.setFloat("Shininess", 15f) // [1,128]

        val hauteurSol = _hauteurSol
        val hauteurMurs = _hauteurMurs

        val box = new Box(1f, _hauteurMurs, 1f)
        val geometry = new Geometry(Configuration.RockStartName + "_" + x.toString + "_" + z.toString, box)
        TangentBinormalGenerator.generate(geometry)
        geometry.setMaterial(mat)
        geometry.move(x * 2, hauteurSol + hauteurMurs, z * 2)
        val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(geometry)

        val control = new RigidBodyControl(sceneShape, 0)
        geometry.addControl(control)
        BulletAppState.getPhysicsSpace.add(control)
        _modele.Shootables.attachChild(geometry)

        _walls.Enable(x, z, geometry, control)
    }

    private val _cote = Configuration.Cote
    var BulletAppState: BulletAppState = null
    private val _walls = new Walls
    private var ground: Geometry = null
    private var playerCC: CharacterControl = null
    private var curseur: Geometry = null
    private var couleurCurseurRouge: Material = null
    private var couleurCurseurVert: Material = null
    private var crossHair: BitmapText = null
    private var _textZone: BitmapText = null
    private var _textZone2: BitmapText = null
    private var autorisationCreuser = false
    private var compteurCurseur = false
    private var totalTime: Long = System.currentTimeMillis
    private val walkDirection = new Vector3f
    private var _coeurDeath: ParticleEmitter = null
    private var _minionDeaths: Array[ParticleEmitter] = null
    private var _minionDeathSuivante: Int = 0
    private var _deathsTimer = mutable.Map[Int, Float]()
    private var lastFrame: Long = modCurrentTime

    // Raccourcis
    private val _hauteurMurs = Configuration.HauteurMurs
    private val _hauteurSol = Configuration.HauteurSol

    def modCurrentTime = _modele.CurrentTime
    def modShootables = _modele.Shootables
    def modGameType = _modele.GameType
    def _modAirDeJeu = _modele.AirDeJeu

    // Adaptation MVC
    def InputManager = inputManager
    def AssetManager = assetManager
    def FlyCam = flyCam

    // Contrôles
    private var left = false
    private var right = false
    private var up = false
    private var down = false
    private var run = false
    private var crushed = false

    // Audio
    private var _audio: Audio = null

    def simpleInitApp() {
        _modele.Init()
    }
    override def simpleUpdate(tpf: Float) {
        def testCurseur() {
            //TODO: Code coloration curseur parait fragile
            if (curseur.getWorldTranslation.getX < 0
                    || curseur.getWorldTranslation.getZ < 0
                    || curseur.getWorldTranslation.getX >= _cote * 2
                    || curseur.getWorldTranslation.getZ >= _cote * 2) {
                autorisationCreuser = false
                curseur.setMaterial(couleurCurseurRouge)
            }
            else {
                autorisationCreuser = true
                if (compteurCurseur && _modele.CurrentTime - totalTime >= 0) {
                    compteurCurseur = false
                    curseur.setMaterial(couleurCurseurVert)
                }
                if (!compteurCurseur)
                    curseur.setMaterial(couleurCurseurVert)
            }
        }
        def actionFps() {
            //if (!left && !right && !up && !down) return // pas de mouvement

            var vitesse = 0.12f
            if (run) vitesse *= 2
            val camDir: Vector3f = cam.getDirection.clone().multLocal(vitesse)
            val camLeft: Vector3f = cam.getLeft.clone().multLocal(0.08f)

            walkDirection.set(0, 0, 0)
            if (left)  walkDirection.addLocal(camLeft)
            if (right) walkDirection.addLocal(camLeft.negate())
            if (up)    walkDirection.addLocal(camDir)
            if (down)  walkDirection.addLocal(camDir.negate())
            playerCC.setWalkDirection(walkDirection)

            val position = playerCC.getPhysicsLocation
            cam.setLocation(position)
            _modele.Player.Geometry.setLocalTranslation(position)
        }
        def actionStr() {
            //if (!left && !right && !up && !down) return // pas de mouvement

            val camDir: Vector3f = cam.getDirection.normalize().mult(0.5f)
            if (left) cam.setLocation(cam.getLocation.add(camDir.getZ, 0, -camDir.getX))
            if (right) cam.setLocation(cam.getLocation.add(-camDir.getZ, 0, camDir.getX))
            if (up) cam.setLocation(cam.getLocation.add(camDir.getX, 0, camDir.getZ))
            if (down) cam.setLocation(cam.getLocation.add(-camDir.getX, 0, -camDir.getZ))
        }
        def VerifieText() {
            _textZone2.setText(_modele.GiveText())
        }
        def VerifieDeathTimers() {
            for ((key,value) <- _deathsTimer) {
                _deathsTimer(key) = _deathsTimer(key) - tpf
                if (_deathsTimer(key) <= 0) {
                    _minionDeaths(key).removeFromParent()
                    _deathsTimer = _deathsTimer - key
                    _minionDeathSuivante -= 1
                }
            }
        }

        // Controle de calcul par secondes limité à 60/sec
        val currentTime = modCurrentTime
        if (currentTime - lastFrame < 16.6f) return
        else lastFrame = currentTime

        // Sorte de controlleur
        testCurseur()
        if (_modele.GameType == "FPS") actionFps()
        else actionStr()

        // Modele
        _modele.SimpleUpdate()
        VerifieText()
        VerifieDeathTimers()
    }

    def InitPhysics() {
        BulletAppState = new BulletAppState()
        stateManager.attach(BulletAppState)
        //BulletAppState.getPhysicsSpace.enableDebug(assetManager)  // Debugger
    }
    def InitBlocks(table: Array[Array[Case]]) {
        def CreateBlocks() {
            val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
            mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"))
            mat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"))
            mat.setBoolean("UseMaterialColors", true)
            mat.setColor("Specular", ColorRGBA.White)
            mat.setColor("Diffuse", ColorRGBA.White)
            mat.setFloat("Shininess", 15f) // [1,128]

            val hauteurSol = _hauteurSol
            val hauteurMurs = _hauteurMurs

            for (x <- 0 until _cote ; y <- 0 until _cote) {
                val box = new Box(1f, _hauteurMurs, 1f)
                if (table(x)(y).Mur) {

                    val geometry = new Geometry(Configuration.RockStartName + "_" + x.toString + "_" + y.toString, box)
                    TangentBinormalGenerator.generate(geometry)
                    geometry.setMaterial(mat)
                    geometry.move(x * 2, hauteurSol + hauteurMurs, y * 2)
                    val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(geometry)

                    val control = new RigidBodyControl(sceneShape, 0)
                    geometry.addControl(control)
                    BulletAppState.getPhysicsSpace.add(control)
                    _modele.Shootables.attachChild(geometry)

                    _walls.Enable(x, y, geometry, control)
                }
            }
        }

        CreateBlocks()
    }
    def InitWall() {
        def CreateMaterial:Material = {
            val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
            //TODO: répéter la texture plutôt que l'étirer
            material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg"))
            material.setColor("Specular", ColorRGBA.White)
            material.setColor("Diffuse", ColorRGBA.White)
            material.setFloat("Shininess", 30f) // [1,128]
            material
        }
        val material = CreateMaterial

        def CreatePhysics(mur: Geometry) {
                val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(mur)
                val landscape = new RigidBodyControl(sceneShape, 0)
                mur.addControl(landscape)
                BulletAppState.getPhysicsSpace.add(landscape)
        }
        def IncrusteWall(mur: Geometry) {
            TangentBinormalGenerator.generate(mur)
            mur.setMaterial(material)
            modShootables.attachChild(mur)
        }

        val murs: Array[Geometry] = Array(
            new Geometry("Wall_NW", new Box(_cote, _hauteurMurs, _cote)),
            new Geometry("Wall_NE", new Box(_cote, _hauteurMurs, _cote * 3)),
            new Geometry("Wall_SW", new Box(_cote, _hauteurMurs, _cote * 3)),
            new Geometry("Wall_SE", new Box(_cote, _hauteurMurs, _cote)) )
        val hauteur = _hauteurSol + _hauteurMurs
        val vecteurs: Array[Vector3f] = Array(
            new Vector3f(_cote -1.0f,     hauteur,   3*_cote -1.0f),
            new Vector3f(3*_cote-1.0f,    hauteur,   _cote -1.0f),
            new Vector3f(-_cote -1.0f,    hauteur,   _cote -1.0f),
            new Vector3f(_cote -1.0f,     hauteur,   -_cote -1.0f) )

        murs.foreach(IncrusteWall)
        for (i <- 0 until murs.length)
            murs(i).move(vecteurs(i))
        murs.foreach(CreatePhysics)
    }
    def InitGround() {
        val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg"))
        mat.setColor("Specular", ColorRGBA.White)
        mat.setColor("Diffuse", ColorRGBA.White)
        mat.setFloat("Shininess", 5f) // [1,128]

        val cote = _cote + 2
        val box = new Box(cote, 1f, cote)
        ground = new Geometry(Configuration.GroundName, box)
        TangentBinormalGenerator.generate(ground) // for lighting effect
        ground.setMaterial(mat)
        modShootables.attachChild(ground)
        ground.move(cote -3, -1, cote -3)

        val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(ground)
        val landscape = new RigidBodyControl(sceneShape, 0)
        ground.addControl(landscape)
        BulletAppState.getPhysicsSpace.add(landscape)
    }
    def InitLight() {
        val al = new AmbientLight()
        al.setColor(ColorRGBA.White.mult(1.5f))
        rootNode.addLight(al)

        def eclairage(direction: Vector3f, force: Float) {
            val eclairage = new DirectionalLight()
            eclairage.setDirection(direction.normalizeLocal())
            eclairage.setColor(ColorRGBA.White.mult(force))
            rootNode.addLight(eclairage)
        }

        val eclairages = List(
            (new Vector3f(1, -2, 1), 0.8f),
            (new Vector3f(-1, -2, 1), 0.4f),
            (new Vector3f(1, -2, -1), 0.5f)
        )

        eclairages.foreach((eclairage _).tupled)
    }
    def InitWorld() {
        rootNode.attachChild(modShootables)
    }
    def InitEntite(entites: GestionnaireEntite, nom: String, point: (Int, Float, Int)) {
        val geometry = entites.Entites(nom).Geometry
        geometry.move(point._1 *2,
                      (point._2 + geometry.getLocalScale.getY) *2,
                      point._3 *2 )
        entites.AddPhysics(nom, BulletAppState)
        modShootables.attachChild(geometry)
    }
    def InitCam() {
        cam.setLocation(new Vector3f(-6, 20, -6))
        cam.lookAt(new Vector3f(10, 0, 10), new Vector3f(0, 1, 0))
    }
    def InitCurseur() {
        def InitCouleurs() {
            couleurCurseurVert = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            couleurCurseurVert.setColor("Color", new ColorRGBA(0, 1, 0, 0.5f))
            couleurCurseurVert.getAdditionalRenderState.setBlendMode(BlendMode.Alpha)

            couleurCurseurRouge = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            couleurCurseurRouge.setColor("Color", new ColorRGBA(1, 0, 0, 0.5f))
            couleurCurseurRouge.getAdditionalRenderState.setBlendMode(BlendMode.Alpha)
        }

        InitCouleurs()

        val hauteurCurseur = 0.1f
        val box = new Box(1, hauteurCurseur, 1)
        curseur = new Geometry("Box", box)
        curseur.setMaterial(couleurCurseurVert)
        curseur.setQueueBucket(Bucket.Transparent)
        rootNode.attachChild(curseur)
        curseur.move(0, _hauteurSol + _hauteurMurs*2 + hauteurCurseur *2, 0)
    }
    def InitCrossHair() {
        //guiNode.detachAllChildren()
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt")
        crossHair = new BitmapText(guiFont, false)
        crossHair.setSize(guiFont.getCharSet.getRenderedSize * 2)
        crossHair.setText("+")         // crosshairs
        crossHair.setLocalTranslation( // center
            settings.getWidth / 2 - guiFont.getCharSet.getRenderedSize / 3 * 2,
            settings.getHeight / 2 + crossHair.getLineHeight / 2, 0)
    }
    def InitPlayer() {
        val capsuleShape = new CapsuleCollisionShape(Configuration.PlayerX, Configuration.PlayerY, 1)
        playerCC = new CharacterControl(capsuleShape, 0.05f)
        playerCC.setJumpSpeed(0)
        playerCC.setFallSpeed(0)
        playerCC.setGravity(0)
        BulletAppState.getPhysicsSpace.add(playerCC)
    }
    def InitKeys() {
        flyCam.setMoveSpeed(0)
        flyCam.setDragToRotate(true)
        //flyCam.setRotationSpeed(0)

        // TODO: Réorganiser ces affectations. Un tableau/liste ou autre, une fonction et un appel pour appliquer le tout
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP))
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN))
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT))
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT))
        inputManager.addMapping("Z", new KeyTrigger(KeyInput.KEY_Z))
        inputManager.addMapping("S", new KeyTrigger(KeyInput.KEY_S))
        inputManager.addMapping("Q", new KeyTrigger(KeyInput.KEY_Q))
        inputManager.addMapping("D", new KeyTrigger(KeyInput.KEY_D))
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE))
        inputManager.addMapping("Return", new KeyTrigger(KeyInput.KEY_RETURN))
        inputManager.addMapping("LShift", new KeyTrigger(KeyInput.KEY_LSHIFT))
        inputManager.addMapping("LControl", new KeyTrigger(KeyInput.KEY_LCONTROL))
        inputManager.addMapping("A", new KeyTrigger(KeyInput.KEY_A))

        inputManager.addMapping("LClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT))

        Array("Up", "Down", "Left", "Right", "Z", "S", "Q", "D",
            "Space", "Return", "LShift", "LControl", "A", "LClick")
            .foreach(inputManager.addListener(actionListener, _))
    }
    def InitInterface() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt")

        _textZone = new BitmapText(guiFont, false)
        _textZone.setSize(guiFont.getCharSet.getRenderedSize)
        _textZone.setColor(ColorRGBA.Green)
        _textZone.setText("resistance du Coeur du donjon :\n" +
                          "Bad minions dans le chateau :\n\n" +
                          "Or :\n" +
                          "Player life\n" +
                          "Player position"
        )
        _textZone.setLocalTranslation(10, settings.getHeight-10, 0)
        guiNode.attachChild(_textZone)

        _textZone2 = new BitmapText(guiFont, false)
        _textZone2.setSize(guiFont.getCharSet.getRenderedSize)
        _textZone2.setColor(ColorRGBA.Red)
        _textZone2.setText("")
        _textZone2.setLocalTranslation(_textZone.getLineWidth + 10, settings.getHeight-10, 0)
        guiNode.attachChild(_textZone2)

    }
    def InitAudio() {
        _audio = new Audio(assetManager, rootNode)
    }


    private val actionListener = new ActionListener {
        def launchCompteurCurseur() {
            compteurCurseur = true
            totalTime = modCurrentTime + 100
        }
        def onAction(name: String, keyPressed: Boolean, tpf: Float) {
            if (modGameType == "STR") {
                //TODO: Empecher le curseur de sortir du l'aire de jeu
                if (name.equals("Z") && !keyPressed) {
                    val vecteurCamera = cam.getDirection.normalize
                    if (Math.abs(vecteurCamera.getX) >= Math.abs(vecteurCamera.getZ)) {
                        if (vecteurCamera.getX >= 0)
                            curseur.move(2, 0, 0)
                        else
                            curseur.move(-2, 0, 0)
                    }
                    else {
                        if (vecteurCamera.getZ >= 0)
                            curseur.move(0, 0, 2)
                        else
                            curseur.move(0, 0, -2)
                    }
                }
                else if (name.equals("S") && !keyPressed) {
                    val vecteurCamera = cam.getDirection.normalize
                    if (Math.abs(vecteurCamera.getX) >= Math.abs(vecteurCamera.getZ)) {
                        if (vecteurCamera.getX >= 0)
                            curseur.move(-2, 0, 0)
                        else
                            curseur.move(2, 0, 0)
                    }
                    else {
                        if (vecteurCamera.getZ >= 0)
                            curseur.move(0, 0, -2)
                        else
                            curseur.move(0, 0,  2)
                    }
                }
                if (name.equals("Q") && !keyPressed) {
                    val vecteurCamera = cam.getDirection.normalize
                    if (Math.abs(vecteurCamera.getX) >= Math.abs(vecteurCamera.getZ)) {
                        if (vecteurCamera.getX >= 0)
                            curseur.move(0, 0, -2)
                        else
                            curseur.move(0, 0, 2)
                    }
                    else {
                        if (vecteurCamera.getZ >= 0)
                            curseur.move(2, 0, 0)
                        else
                            curseur.move(-2, 0, 0)
                    }

                }
                else if (name.equals("D") && !keyPressed) {
                    val vecteurCamera = cam.getDirection.normalize
                    if (Math.abs(vecteurCamera.getX) >= Math.abs(vecteurCamera.getZ)) {
                        if (vecteurCamera.getX >= 0)
                            curseur.move(0, 0, 2)
                        else
                            curseur.move(0, 0, -2)
                    }
                    else {
                        if (vecteurCamera.getZ >= 0)
                            curseur.move(-2, 0, 0)
                        else
                            curseur.move(2, 0, 0)
                    }
                }
                if (name.equals("Up")) {
                    up = keyPressed
                }
                else if (name.equals("Down")) {
                    down = keyPressed
                }
                if (name.equals("Left")) {
                    left = keyPressed
                }
                else if (name.equals("Right")) {
                    right = keyPressed
                }
                if (name.equals("Space") && !keyPressed && autorisationCreuser) {
                    curseur.setMaterial(couleurCurseurRouge)
                    launchCompteurCurseur()

                    val x = (curseur.getWorldTranslation.getX/2).toInt
                    val z = (curseur.getWorldTranslation.getZ/2).toInt

                    if (_walls.Disable(x, z))
                        _modele.RemoveMur(x, z)
                    _audio.PlayBang()
                }
                if (name.equals("Return") && !keyPressed) {
                    _modele.SwitchGamePlay()
                }
                if (name.equals("A") && !keyPressed) {
                    val x = (curseur.getWorldTranslation.getX/2).toInt
                    val z = (curseur.getWorldTranslation.getZ/2).toInt

                    if (!_walls.Existe(x, z)) {
                        _modele.CreateMur(x, z)
                        _audio.PlayBang()
                    }
                }
            }
            else if (_modele.GameType == "FPS") {
                if (name.equals("Z")) {
                    up = keyPressed
                }
                else if (name.equals("S")) {
                    down = keyPressed
                }
                if (name.equals("Q")) {
                    left = keyPressed
                }
                else if (name.equals("D")) {
                    right = keyPressed
                }
                if (name.equals("Space")) { playerCC.jump() }
                if (name.equals("LShift")) { run = keyPressed }
                if (name.equals("LControl")) { crushed = keyPressed }   // TODO: S'accroupir ne fonctionne pas
                if (name.equals("LClick") && !keyPressed) {
                    _audio.PlayGun()
                    val results = new CollisionResults                      // 1. Reset results list.
                    val ray = new Ray(cam.getLocation, cam.getDirection)    // 2. Aim the ray from cam loc to cam direction.
                    modShootables.collideWith(ray, results)                 // 3. Collect intersections between Ray
                                                                            //    and Shootables in results list.
                    if (results.size() > 1) {                               // 4. Use the results (we mark the hit object)
                        val closest: CollisionResult = results.getCollision(1)
                        if(closest.getGeometry.getName.substring(0, 3) == Configuration.MinionStartName)
                            _modele.ShootAt(closest.getGeometry.getName)
                        else
                            _modele.Marks.GiveResults(closest.getContactPoint)
                    }
                }
                if (name.equals("Return") && !keyPressed) {
                    _modele.SwitchGamePlay()
                }
            }
        }
    }
    def SwitchFps() {
        def InitPlayer() {
            val position = new Vector3f(
                curseur.getWorldTranslation.getX,
                curseur.getWorldTranslation.getY,
                curseur.getWorldTranslation.getZ )
            cam.setLocation(position)
            cam.lookAt(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0))
            playerCC.setPhysicsLocation(position)

            playerCC.setJumpSpeed(10)
            playerCC.setFallSpeed(30)
            playerCC.setGravity(30)

            modShootables.attachChild(_modele.Player.Geometry)
        }
        def Pointeur() {
            guiNode.attachChild(crossHair)
            curseur.removeFromParent()
        }
        def ModifKeys() {
            flyCam.setDragToRotate(false)
        }
        def ChangeGameType() {
            _modele.GameType = "FPS"
        }

        ChangeGameType()
        InitPlayer()
        Pointeur()
        ModifKeys()
    }
    def SwitchStr() {
        def Pointeur() {
            crossHair.removeFromParent()
            rootNode.attachChild(curseur)
        }
        def InitPlayer() {
            cam.setLocation(new Vector3f(-6, 20, -6))
            cam.lookAt(new Vector3f(10, 0, 10), new Vector3f(0, 1, 0))
            playerCC.setJumpSpeed(20)
            playerCC.setFallSpeed(30)
            playerCC.setGravity(30)
            _modele.Player.Geometry.removeFromParent()
            up=false ; down=false ; left=false ; right=false

        }
        def ModifKeys() {
            flyCam.setDragToRotate(true)
        }
        def ChangeGameType() {
            _modele.GameType = "STR"
        }
        ChangeGameType()
        InitPlayer()
        Pointeur()
        ModifKeys()
    }

    def Affiche(mark: Geometry) {
        rootNode.attachChild(mark)
    }
    def Move(personnage: Personnage) {
        if (personnage.left) {
            personnage.Geometry.setLocalTranslation(personnage.Geometry.getLocalTranslation.add(0, 0, -0.1f * personnage.Speed))
            _modele.Move(personnage)
        }
        if (personnage.right) {
            personnage.Geometry.setLocalTranslation(personnage.Geometry.getLocalTranslation.add(0, 0, 0.1f * personnage.Speed))
            _modele.Move(personnage)
        }
        if (personnage.up) {
            personnage.Geometry.setLocalTranslation(personnage.Geometry.getLocalTranslation.add(0.1f, 0, 0 * personnage.Speed))
            _modele.Move(personnage)
        }
        if (personnage.down) {
            personnage.Geometry.setLocalTranslation(personnage.Geometry.getLocalTranslation.add(-0.1f, 0, 0 * personnage.Speed))
            _modele.Move(personnage)
        }
    }
    def GiveWallAutour(point: (Int, Int)): Array[Array[Boolean]] = {
        val autour = Array.ofDim[Boolean](3, 3)
        val x = point._1
        val z = point._2
        for (i <- -1 to +1 ; j <- -1 to +1) {
            if (x + i < 0
                || z + j < 0
                || x + i >= _cote
                || z + j >= _cote ) {
                autour(i+1)(j+1) = true
            }
            else if (_walls.Existe(x+i, z+j)) {
                autour(i+1)(j+1) = true
            }
            else
                autour(i+1)(j+1) = false
        }
        autour
    }
    def VerifieNoWall(point: (Int, Int)): Boolean = {
        _walls.Existe(point._1, point._2)
    }
    def InitCoeurDeath() {
        _coeurDeath = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30)
        val mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
        mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"))
        _coeurDeath.setMaterial(mat_red)
        _coeurDeath.setImagesX(2)
        _coeurDeath.setImagesY(2) // 2x2 texture animation
        _coeurDeath.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f))     // red
        _coeurDeath.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)) // yellow
        _coeurDeath.getParticleInfluencer.setInitialVelocity(new Vector3f(0, 2, 0))
        _coeurDeath.setStartSize(1.5f)
        _coeurDeath.setEndSize(0.1f)
        _coeurDeath.setGravity(0, 0, 0)
        _coeurDeath.setLowLife(1f)
        _coeurDeath.setHighLife(3f)
        _coeurDeath.getParticleInfluencer.setVelocityVariation(0.3f)
    }
    def InitMinionDeath() {
        val minionDeathsLength = Configuration.MaxMinions
        _minionDeaths = new Array[ParticleEmitter](minionDeathsLength)
        for(i <- 0 until minionDeathsLength) {
            _minionDeaths(i) = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30)
            val mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
            mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"))
            _minionDeaths(i).setMaterial(mat_red)
            _minionDeaths(i).setImagesX(1)
            _minionDeaths(i).setImagesY(1) // 2x2 texture animation
            _minionDeaths(i).setEndColor(new ColorRGBA(0f, 1f, 0f, 1f))     // red
            _minionDeaths(i).setStartColor(new ColorRGBA(0.7f, 0.7f, 1f, 0.5f)) // yellow
            _minionDeaths(i).getParticleInfluencer.setInitialVelocity(new Vector3f(0, 2, 0))
            _minionDeaths(i).setStartSize(1.5f)
            _minionDeaths(i).setEndSize(0.1f)
            _minionDeaths(i).setGravity(0, 0, 0)
            _minionDeaths(i).setLowLife(1f)
            _minionDeaths(i).setHighLife(3f)
            _minionDeaths(i).getParticleInfluencer.setVelocityVariation(0.3f)
        }
        _minionDeathSuivante = 0
    }
    def PlayCoeurDeath(lieu: Vector3f) {
        _coeurDeath.setLocalTranslation(lieu)
        rootNode.attachChild(_coeurDeath)

        val text = new BitmapText(guiFont, false)
        text.setSize(guiFont.getCharSet.getRenderedSize * 3)
        text.setColor(ColorRGBA.Red)
        text.setText("Le coeur est détruit.\nVous avez perdu")
        text.setLocalTranslation( // center
            settings.getWidth / 2 - text.getLineWidth/2,
            settings.getHeight / 2 + crossHair.getLineHeight / 2, 0)

        guiNode.attachChild(text)
    }
    def PlayMinionDeath(lieu: Vector3f) {
        _minionDeaths(_minionDeathSuivante).setLocalTranslation(lieu)
        rootNode.attachChild(_minionDeaths(_minionDeathSuivante))
        _deathsTimer(_minionDeathSuivante) = Configuration.MinionDeathTime

        _minionDeathSuivante += 1
    }
    def PlayerPosition = cam.getLocation
    def PLayPlayerDeath() {
        _modele.KillPlayer()
        SwitchStr()
    }
}
