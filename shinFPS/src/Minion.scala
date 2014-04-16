import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.scene.Geometry

/**
 * Created by shinmox on 01/04/14.
 */
class Minion(Geometry: Geometry, _modele: Modele, Nom: String, _material: Material)
    extends Personnage(Geometry, _modele, Nom) {
    Force = 1
    PointVie = 3

    override def RecoitFrappe(quantite: Int) {
        _material.setColor("Color", ColorRGBA.Brown)
        Geometry.setMaterial(_material)
        super.RecoitFrappe(quantite)
    }
}
