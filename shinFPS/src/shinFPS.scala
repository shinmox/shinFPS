import com.jme3.bullet.BulletAppState
import com.jme3.bullet.collision.shapes.{CapsuleCollisionShape, CollisionShape}
import com.jme3.bullet.control.{CharacterControl, RigidBodyControl}
import com.jme3.bullet.util.CollisionShapeFactory
import com.jme3.collision.{CollisionResult, CollisionResults}
import com.jme3.font.BitmapText
import com.jme3.input.controls.{MouseButtonTrigger, ActionListener, KeyTrigger}
import com.jme3.input.{MouseInput, ChaseCamera, KeyInput}
import com.jme3.light.{AmbientLight, DirectionalLight}
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.{Ray, ColorRGBA, Vector3f}
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.scene.{Node, Geometry}
import com.jme3.scene.shape.Box
import com.jme3.util.TangentBinormalGenerator
import com.jme3.app.SimpleApplication
import com.jme3.material.Material
import Entite.GestionnaireEntite

/**
 * Created by shinmox on 25/03/14.
 */
object shinFPS
{
  /** main
    *
    * @param args non utilisé
    */
  def main(args: Array[String]) {
    val app = new shinFPS
    app.start()
  }
}

class shinFPS extends SimpleApplication {
    //TODO: Créer une classe ennemi
    //TODO: Mettre en place des créations d'obstacle
    //TODO: Tir dans les murs exterieurs
    //TODO: Effacer les tirs lorsqu'une brique est retirée
    //TODO: Ajouter une zone coeur au centre de la pièce servant de lieu de décès (defense tower like)
    //TODO: Régler la lumière
    //TODO: IA permettant d'aller au centre pour détruire le donjon
    var gameType: String = "STR"
    val NombreCube = 400 // Racine carré doit être fini

    // creation sol
    var curseur: Geometry = null
    var compteurCurseur = false
    var couleurCurseurRouge: Material = null
    var couleurCurseurVert: Material = null
    var totalTime: Long = System.currentTimeMillis
    var currentTime: Long = 0
    val rocks: Array[Geometry] = new Array[Geometry](NombreCube)
    var ground: Geometry = null
    var chaseCam: ChaseCamera = null
    var autorisationCreuser = false

    // collision
    private var bulletAppState: BulletAppState = null
    private var player: CharacterControl = null
    private val walkDirection = new Vector3f
    private val rockControls = new Array[RigidBodyControl](NombreCube)
    private var left = false
    private var right = false
    private var up = false
    private var down = false
    private var run = false
    private var crushed = false

    // Tir
    var shootables: Node = null
    var marks: Marker = null
    var crossHair: BitmapText = null

    // Entites
    val entites: GestionnaireEntite = new GestionnaireEntite

    def simpleInitApp() {
        def TestParametres() {
            if (NombreCube % Math.sqrt(NombreCube) != 0)
                throw new Exception("NombreCube ne représente pas un carré")
        }
        def InitPhysics() {
            bulletAppState = new BulletAppState()
            stateManager.attach(bulletAppState)
            //bulletAppState.getPhysicsSpace.enableDebug(assetManager)  // Debugger
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
            inputManager.addMapping("LClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
            Array("Up", "Down", "Left", "Right", "Z", "S", "Q", "D",
                    "Space", "Return", "LShift", "LControl", "LClick")
                .foreach(inputManager.addListener(actionListener, _))
        }
        def InitWorld() {
            def InitBlocks() {
                def CreateBlocks() {
                    val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
                    mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"))
                    mat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"))
                    mat.setBoolean("UseMaterialColors", true)
                    mat.setColor("Specular", ColorRGBA.White)
                    mat.setColor("Diffuse", ColorRGBA.White)
                    mat.setFloat("Shininess", 15f) // [1,128]

                    for (i <- 0 until rocks.length) {
                        val box = new Box(1f, 2f, 1f)
                        rocks(i) = new Geometry("Shiny rock", box)
                        TangentBinormalGenerator.generate(rocks(i)) // for lighting effect
                        rocks(i).setMaterial(mat)
                    }
                }
                def MoveBlocks() {
                    val xmax = Math.sqrt(NombreCube).toInt
                    for (i <- 0 until xmax) {
                        for (j <- 0 until xmax) {
                            rocks(i * xmax + j).move(i * 2, 2.5f, j * 2)
                        }
                    }
                }
                def CreatePhysic() {
                    for (i <- 0 until rocks.length) {
                        val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(rocks(i))
                        rockControls(i) = new RigidBodyControl(sceneShape, 0)
                        rocks(i).addControl(rockControls(i))
                        bulletAppState.getPhysicsSpace.add(rockControls(i))
                    }
                }
                def RemoveEniZoneBlocks() {

                }

                CreateBlocks()
                MoveBlocks()
                CreatePhysic()
                rocks.foreach(shootables.attachChild)
                RemoveEniZoneBlocks()

            }
            def InitUndestructible() {
                def InitWall() {
                    val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
                    val texture = assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg")
                    //TODO: répéter la texture
                    mat.setTexture("DiffuseMap", texture)
                    mat.setColor("Specular", ColorRGBA.White)
                    mat.setColor("Diffuse", ColorRGBA.White)
                    mat.setFloat("Shininess", 5f) // [1,128]

                    val cote = Math.sqrt(NombreCube).toInt
                    val walls: Array[Geometry] = Array(
                        new Geometry("Wall_NW", new Box(cote, 2f, cote)),
                        new Geometry("Wall_NE", new Box(cote, 2f, cote * 3)),
                        new Geometry("Wall_SW", new Box(cote, 2f, cote * 3)),
                        new Geometry("Wall_SE", new Box(cote, 2f, cote))
                    )

                    walls.foreach(TangentBinormalGenerator.generate)
                    walls.foreach(_.setMaterial(mat))
                    walls.foreach(rootNode.attachChild)

                    walls(0).move(cote -1,     2.5f,   3*cote -1) // NE
                    walls(1).move(3*cote-1,    2.5f,   cote -1)   // NW
                    walls(2).move(-cote -1,    2.5f,   cote -1)   // SW
                    walls(3).move(cote -1 ,    2.5f,   -cote -1)  // SE

                    def createWallPhysic(wall: Geometry) {
                        val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(wall)
                        val landscape = new RigidBodyControl(sceneShape, 0)
                        wall.addControl(landscape)
                        bulletAppState.getPhysicsSpace.add(landscape)
                    }
                    walls.foreach(createWallPhysic)
                }
                def InitGround() {
                    val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
                    mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg"))
                    mat.setColor("Specular", ColorRGBA.White)
                    mat.setColor("Diffuse", ColorRGBA.White)
                    mat.setFloat("Shininess", 5f) // [1,128]

                    val cote = Math.sqrt(NombreCube).toInt + 2
                    val box = new Box(cote, 1f, cote)
                    ground = new Geometry("Shiny rock", box)
                    TangentBinormalGenerator.generate(ground) // for lighting effect
                    ground.setMaterial(mat)
                    rootNode.attachChild(ground)
                    ground.move(cote -3, -0.5f, cote-3)

                    // We set up collision detection for the scene by creating a
                    // compound collision shape and a static RigidBodyControl with mass zero.
                    val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(ground)
                    val landscape = new RigidBodyControl(sceneShape, 0)
                    ground.addControl(landscape)
                    bulletAppState.getPhysicsSpace.add(landscape)
                }

                InitGround()
                InitWall()
            }
            def CreateLight() {
                val al = new AmbientLight()
                al.setColor(ColorRGBA.White.mult(1.3f))
                rootNode.addLight(al)

                val sun = new DirectionalLight()
                sun.setDirection(new Vector3f(0, -10, 0).normalizeLocal())
                sun.setColor(ColorRGBA.White)
                rootNode.addLight(sun)
            }

            shootables = new Node("Shootables")
            InitUndestructible()
            InitBlocks()
            CreateLight()
            rootNode.attachChild(shootables)
        }
        def InitEntites() {
            //Create
        }
        def InitMarks() {
            marks = new Marker(5, assetManager, this)
        }
        def MoveCam() {
            cam.setLocation(new Vector3f(-6, 20, -6))
            cam.lookAt(new Vector3f(10, 0, 10), new Vector3f(0, 1, 0))
        }
        def InitPointeurs() {
            def InitCurseur() {
                def InitCouleurs() {
                    couleurCurseurVert = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
                    couleurCurseurVert.setColor("Color", new ColorRGBA(0, 1, 0, 0.5f))
                    couleurCurseurVert.getAdditionalRenderState.setBlendMode(BlendMode.Alpha)

                    couleurCurseurRouge = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
                    couleurCurseurRouge.setColor("Color", new ColorRGBA(1, 0, 0, 0.5f))
                    couleurCurseurRouge.getAdditionalRenderState.setBlendMode(BlendMode.Alpha)
                }

                val box = new Box(1, 0.1f, 1)
                curseur = new Geometry("Box", box)
                curseur.setMaterial(couleurCurseurVert)
                curseur.setQueueBucket(Bucket.Transparent)
                rootNode.attachChild(curseur)
                curseur.move(0, 4.6f, 0)

                InitCouleurs()
            }
            def InitCrossHair() {
                guiNode.detachAllChildren()
                guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt")
                crossHair = new BitmapText(guiFont, false)
                crossHair.setSize(guiFont.getCharSet.getRenderedSize * 2)
                crossHair.setText("+");        // crosshairs
                crossHair.setLocalTranslation( // center
                    settings.getWidth / 2 - guiFont.getCharSet.getRenderedSize / 3 * 2,
                    settings.getHeight / 2 + crossHair.getLineHeight / 2, 0)
            }

            InitCurseur()
            InitCrossHair()
        }
        def InitPlayer() {
            val capsuleShape = new CapsuleCollisionShape(0.5f, 1f, 1)
            player = new CharacterControl(capsuleShape, 0.05f)
            player.setJumpSpeed(0)
            player.setFallSpeed(0)
            player.setGravity(0)
            bulletAppState.getPhysicsSpace.add(player)
        }

        TestParametres()
        InitPhysics()
        InitKeys()
        InitWorld()
        InitEntites()
        InitMarks()
        MoveCam()
        InitPointeurs()
        InitPlayer()
    }
    override def simpleUpdate(tpf: Float) {
        def testCurseur() {
            //TODO: Code coloration curseur fragile
            val cote = Math.sqrt(NombreCube)
            if (curseur.getWorldTranslation.getX < 0
                    || curseur.getWorldTranslation.getZ < 0
                    || curseur.getWorldTranslation.getX >= cote*2
                    || curseur.getWorldTranslation.getZ >= cote*2) {
                autorisationCreuser = false
                curseur.setMaterial(couleurCurseurRouge)
            }
            else {
                autorisationCreuser = true
                currentTime = System.currentTimeMillis
                if (compteurCurseur && currentTime - totalTime >= 0) {
                    compteurCurseur = false
                    curseur.setMaterial(couleurCurseurVert)
                }
                if (!compteurCurseur)
                    curseur.setMaterial(couleurCurseurVert)
            }
        }
        def actionFps() {
            var vitesse = 0.12f
            if (run) vitesse *= 2
            val camDir: Vector3f = cam.getDirection.clone().multLocal(vitesse)
            val camLeft: Vector3f = cam.getLeft.clone().multLocal(0.08f)
            walkDirection.set(0, 0, 0)
            if (left)  walkDirection.addLocal(camLeft)
            if (right) walkDirection.addLocal(camLeft.negate())
            if (up)    walkDirection.addLocal(camDir)
            if (down)  walkDirection.addLocal(camDir.negate())
            player.setWalkDirection(walkDirection)
            cam.setLocation(player.getPhysicsLocation)
        }
        def actionStr() {
            if (left)
                cam.setLocation(cam.getLocation.add(0.35f, 0, -0.35f))
            if (right)
                cam.setLocation(cam.getLocation.add(-0.35f, 0, 0.35f))
            if (up) {
                cam.setLocation(cam.getLocation.add(0.35f, 0, 0.35f))
            }
            if (down)
                cam.setLocation(cam.getLocation.add(-0.35f, 0, -0.35f))
        }

        testCurseur()
        if (gameType == "FPS")
            actionFps()
        else {
            actionStr()
        }
    }

    private def switchGamePlay() {
        if (gameType == "STR") {
            def InitPlayer() {
                cam.setLocation(new Vector3f(
                    curseur.getWorldTranslation.getX,
                    curseur.getWorldTranslation.getY,
                    curseur.getWorldTranslation.getZ ))
                cam.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0))
                player.setPhysicsLocation(new Vector3f(
                    curseur.getWorldTranslation.getX,
                    curseur.getWorldTranslation.getY,
                    curseur.getWorldTranslation.getZ ) )

                player.setJumpSpeed(20)
                player.setFallSpeed(30)
                player.setGravity(30)
            }
            def Pointeur() {
                guiNode.attachChild(crossHair)
                curseur.removeFromParent()
            }
            def ModifKeys() {
                flyCam.setDragToRotate(false)
            }
            def ChangeGameType() {
                gameType = "FPS"
            }

            ChangeGameType()
            InitPlayer()
            Pointeur()
            ModifKeys()
        }
        else {
            def Pointeur() {
                crossHair.removeFromParent()
                rootNode.attachChild(curseur)
            }
            def InitPlayer() {
                cam.setLocation(new Vector3f(-6, 20, -6))
                cam.lookAt(new Vector3f(10, 0, 10), new Vector3f(0, 1, 0))
                player.setJumpSpeed(20)
                player.setFallSpeed(30)
                player.setGravity(30)
            }
            def ModifKeys() {
                flyCam.setDragToRotate(true)
            }
            def ChangeGameType() {
                gameType = "STR"
            }
            ChangeGameType()
            InitPlayer()
            Pointeur()
            ModifKeys()
        }
    }
    private val actionListener = new ActionListener {
        def launchCompteurCurseur() {
            compteurCurseur = true
            totalTime = currentTime + 100
        }
        def onAction(name: String, keyPressed: Boolean, tpf: Float) {
            if (gameType == "STR") {
                //TODO: Empecher le curseur de sortir du l'aire de jeu
                if (name.equals("Z") && !keyPressed) {
                    curseur.move(0, 0, 2)
                }
                if (name.equals("S") && !keyPressed) {
                    curseur.move(0, 0, -2)
                }
                if (name.equals("Q") && !keyPressed) {
                    curseur.move(2, 0, 0)
                }
                if (name.equals("D") && !keyPressed) {
                    curseur.move(-2, 0, 0)
                }
                if (name.equals("Up")) {
                    up = keyPressed
                }
                if (name.equals("Down")) {
                    down = keyPressed
                }
                if (name.equals("Left")) {
                    left = keyPressed
                }
                if (name.equals("Right")) {
                    right = keyPressed
                }
                if (name.equals("Space") && !keyPressed && autorisationCreuser) {
                    curseur.setMaterial(couleurCurseurRouge)
                    launchCompteurCurseur()

                    val x = curseur.getWorldTranslation.getX
                    val z = curseur.getWorldTranslation.getZ
                    val i = (x/2 * Math.sqrt(NombreCube) + z/2).toInt
                    rockControls(i).setEnabled(false)
                    rocks(i).removeFromParent()
                }
                if (name.equals("Return") && !keyPressed) {
                    switchGamePlay()
                }
            }
            else if (gameType == "FPS") {
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
                if (name.equals("Space")) { player.jump() }
                if (name.equals("LShift")) { run = keyPressed }
                if (name.equals("LControl")) { crushed = keyPressed }   // TODO: S'accroupir ne fonctionne pas
                if (name.equals("LClick") && !keyPressed) {
                    val results = new CollisionResults                      // 1. Reset results list.
                    val ray = new Ray(cam.getLocation, cam.getDirection)    // 2. Aim the ray from cam loc to cam direction.
                    shootables.collideWith(ray, results)                    // 3. Collect intersections between Ray
                                                                            //    and Shootables in results list.
                    if (results.size() > 0) {                               // 4. Use the results (we mark the hit object)
                        val closest: CollisionResult = results.getClosestCollision
                        marks.GiveResults(closest.getContactPoint)
                    }
                }
                if (name.equals("Return") && !keyPressed) {
                    switchGamePlay()
                }
            }
        }
    }
    def Affiche(mark: Geometry) {
        rootNode.attachChild(mark)
    }
}
