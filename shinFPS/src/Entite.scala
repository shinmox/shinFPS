import com.jme3.scene.Geometry

/**
 * Created by shinmox on 01/04/14.
 */
abstract class Entite (val Geometry: Geometry, modele: Modele) {
    var pointVie: Int
    var position: (Int, Int, Int) = null

    def InitPosition(positionInitial: (Int, Int, Int)) {
        position = positionInitial
    }
    def PlayIA() {}
}
