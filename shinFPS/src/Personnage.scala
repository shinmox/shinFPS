import com.jme3.scene.Geometry
import scala.collection.mutable

/**
 * Created by shinmox on 01/04/14.
 */
class Personnage (Geometry: Geometry, modele: Modele) extends Entite(Geometry: Geometry, modele: Modele) {
    override var pointVie: Int = _

    var mouvementSpeed: Float = 0
    var distanceVue = 1
    var vision = List[Vu]()

    def Regarde() {
        vision = modele.DonneVision(distanceVue)
    }

}
