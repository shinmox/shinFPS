import com.jme3.scene.Geometry

/**
 * Created by shinmox on 01/04/14.
 */
class Mob(Geometry: Geometry, modele: Modele)
    extends Personnage(Geometry = Geometry, modele = modele) {

    override def PlayIA() {
        TrouveProchainPoint()
    }

    private def TrouveProchainPoint() {

    }
}
