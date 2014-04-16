import com.jme3.bullet.control.RigidBodyControl
import com.jme3.scene.Geometry

/**
 * Created by shinmox on 01/04/14.
 */
abstract class Entite (val Geometry: Geometry, modele: Modele) {
    var PointVie: Int = 0
    var Control: RigidBodyControl = null
}
