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
class GestionnaireEntite {
    val Entites  = mutable.Map[String, Entite]()
    var compteurMob: Int = 0

    def CreateCoeurDuDonjon(assetManager: AssetManager, nom: String,
                           position: (Int, Int, Int), observer: Modele) {
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

        TangentBinormalGenerator.generate(geometry) // for lighting effect
        geometry.setMaterial(material)

        Entites(nom) = new Personnage(geometry, observer)
        Entites(nom).InitPosition(position)
    }

    def AjouterMob(manager: AssetManager, position: (Int, Int, Int), modele: Modele): String = {
        def InitMaterial(): Material = {
            val material = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md")
            material.setColor("Color", new ColorRGBA(1, 0, 0, 1))
            material
        }

        val nom: String = "mob" + compteurMob.toString
        compteurMob += 1

        val material = InitMaterial()
        val box = new Box(0.5f, 0.5f, 0.5f)
        val geometry = new Geometry(nom, box)

        geometry.setMaterial(material)

        Entites(nom) = new Mob(geometry, modele)
        Entites(nom).InitPosition(position)

        nom
    }
    def AddPhysics(nom: String, bulletAppState: BulletAppState) {
        val CDDGeometry = Entites(nom).Geometry
        val sceneShape: CollisionShape = CollisionShapeFactory.createMeshShape(CDDGeometry)
        val CDDControl = new RigidBodyControl(sceneShape, 0)
        CDDGeometry.addControl(CDDControl)
        bulletAppState.getPhysicsSpace.add(CDDGeometry)
    }

    def PlayIA() {
        Entites.values.foreach(_.PlayIA())
    }
}
