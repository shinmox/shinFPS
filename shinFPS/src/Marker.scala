import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Sphere

/**
 * Created by shinmox on 30/03/14.
 */
class Marker(Number: Int, assetManager: AssetManager, observer: Ui) {
    var marks: Array[Geometry] = null
    var compteur: Int = 0
    Marker()

    def Marker() {
        def createMark() = {
            val sphere = new Sphere(5, 5, 0.05f)
            val mark = new Geometry("mark" + compteur.toString, sphere)
            val mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            mark_mat.setColor("Color", ColorRGBA.Red)
            mark.setMaterial(mark_mat)
            mark
        }
        marks = new Array[Geometry](Number)
        for (i <- 0 until Number ) {
            marks(i) = createMark()
            compteur += 1
        }
        compteur = -1
    }
    def GiveResults(position: Vector3f) {
        def nextMark: Geometry = {
            if (compteur == Number - 1)     // On est au max
                compteur = 0                // On modifiera le numero zero
            else
                compteur += 1               // On en ajoute un ou on modifie le plus ancien des 5
            marks(compteur)             // On le retourne
        }
        val next = nextMark
        next.setLocalTranslation(position)
        observer.Affiche(next)
        // TODO: Mettre un timer pour faire disparaître la mark
    }
}
