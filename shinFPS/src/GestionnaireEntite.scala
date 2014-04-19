import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.scene.shape.Box
import com.jme3.scene.Geometry
import com.jme3.util.TangentBinormalGenerator
import com.jme3.asset.AssetManager
import com.jme3.bullet.collision.shapes.CollisionShape
import com.jme3.bullet.util.CollisionShapeFactory
import com.jme3.bullet.control.RigidBodyControl
import scala.collection.mutable
import com.jme3.bullet.BulletAppState

/**
 * Created by shinmox on 01/04/14.
 */
class GestionnaireEntite(_configuration: Configuration) {
    val Entites = mutable.Map[String, Personnage]()
    private val _positions = mutable.Map[String, (Int, Float, Int)]()
    var CompteurMob: Int = 0

    def CreatePlayer(assetManager: AssetManager, nom: String, position: (Int, Float, Int), modele: Modele): Player = {
        def InitMaterial(): Material = {
            val material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            material.setColor("Color", new ColorRGBA(1, 1, 1, 1))
            material
        }

        val nom: String = _configuration.PlayerName

        val material = InitMaterial()
        val box = new Box(_configuration.PlayerX, _configuration.PlayerY, _configuration.PlayerZ)
        val geometry = new Geometry(nom, box)

        geometry.setMaterial(material)

        geometry.move(position._1,
            position._2,
            position._3 )
        val player= new Player(geometry, modele, nom)
        Entites(nom) = player
        _positions(nom) = position
        player
    }
    def CreateCoeurDuDonjon(assetManager: AssetManager, nom: String,
                           position: (Int, Float, Int), observer: Modele) {
        def InitMaterial(): Material = {
            val material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
            material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"))
            material.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"))
            material.setBoolean("UseMaterialColors", true)
            material.setColor("Specular", ColorRGBA.Red)
            material.setColor("Diffuse", ColorRGBA.Red)
            material.setFloat("Shininess", 100f) // Brillant

            material
        }

        val material = InitMaterial()
        val box = new Box(1f, 3f, 1f)
        val geometry = new Geometry(nom, box)

        TangentBinormalGenerator.generate(geometry) //TODO: Du domaine de l'UI
        geometry.setMaterial(material)

        Entites(nom) = new Personnage(geometry, observer, nom)
        _positions(nom) = position
        Entites(nom).PointVie = 20
    }

    def AjouterMob(manager: AssetManager, position: (Int, Float, Int), modele: Modele): String = {
        def InitMaterial(typeMinion: Int): Material = {
            val material = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md")
            if (typeMinion  < 5) {
                material.setColor("Color", new ColorRGBA(1, 0, 0, 1))
            }
            else if (typeMinion > 7) {
                material.setColor("Color", new ColorRGBA(0, 1, 0, 1))
            }
            else {
                material.setColor("Color", new ColorRGBA(0, 0, 1, 1))
            }
            material
        }


        val typeMinion = util.Random.nextInt(10)
        // 0 -> 4 : normal
        // 5 -> 7 : rapide
        // 8 -> 9 : fort et lent

        val nom: String = _configuration.MinionStartName + CompteurMob.toString
        CompteurMob += 1

        val material = InitMaterial(typeMinion)

        var box: Box = null
        if (typeMinion < 8) box = new Box(0.4f, 0.4f, 0.4f)
        else                box = new Box(0.7f, 0.7f, 0.7f)
        val geometry = new Geometry(nom, box)

        geometry.setMaterial(material)

        geometry.move(position._1 * 2,
            position._2 + geometry.getLocalScale.getY,
            position._3 * 2 )
        Entites(nom) = new Minion(geometry, modele, nom, material)

        if (typeMinion > 7) {
            Entites(nom).Armure = _configuration.MinionBigArmure
            Entites(nom).Force = _configuration.MinionBigForce
            Entites(nom).Speed = _configuration.MinionBigSpeed
            Entites(nom).PointVie = _configuration.MinionBigLife
        }
        else if (typeMinion > 4) {
            Entites(nom).Speed = _configuration.MinionSpeedQuick
        }

        _positions(nom) = position
        nom
    }
    def AddPhysics(nom: String, bulletAppState: BulletAppState) {
        val CDDGeometry = Entites(nom).Geometry
        val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(CDDGeometry)
        Entites(nom).Control = new RigidBodyControl(sceneShape, 0)
        CDDGeometry.addControl(Entites(nom).Control)
        bulletAppState.getPhysicsSpace.add(CDDGeometry)
    }

    def Position(nom: String): (Int, Float, Int) = _positions(nom)
    def Kill(nom: String) {
        Entites(nom).Geometry.removeFromParent()
        Entites(nom).Control.setEnabled(false)
        Entites.remove(nom)
        if (nom.substring(0, 3) == _configuration.MinionStartName)
            CompteurMob -= 1
    }

    def MovePhysics(nom: String) {
        Entites(nom).MovePhysic()
    }
}
