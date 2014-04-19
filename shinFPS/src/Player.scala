import com.jme3.scene.Geometry

/**
 * Created by shinmox on 11/04/14.
 */
class Player(geometry: Geometry, modele: Modele, nom: String)
    extends Personnage(geometry, modele, nom) {
    var Gold = 0
    var Degats = modele.Configuration.PlayerDegats
    PointVie = 15
}
