import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry

/**
 * Created by shinmox on 07/04/14.
 */
class Walls (_configuration: Configuration) {
    private val _cote = _configuration.Cote
    private val _walls = Array.ofDim[Wall](_cote, _cote)

    def Enable(x: Int, y: Int, geometry: Geometry, control: RigidBodyControl) {
        _walls(x)(y).Geometry = geometry
        _walls(x)(y).Control = control
        _walls(x)(y).Existe = true
    }
    def Disable(x: Int, y: Int): Boolean = {
        if (_walls(x)(y) == null
            || _walls(x)(y).Geometry == null
            || _walls(x)(y).Geometry.getParent == null)
            return false

        _walls(x)(y).Geometry.removeFromParent()
        _walls(x)(y).Control.setEnabled(false)
        _walls(x)(y).Existe = false
        true
    }
    def Existe(x: Int, z: Int): Boolean = _walls(x)(z).Existe
    def Existe(position: Vector3f): Boolean = {
        var x = (position.getX/2f).toInt
        if (x >= _cote) x = _cote -1
        if (x <= 0) x = 0

        var z = (position.getZ/2f).toInt
        if (z >= _cote) z = _cote -1
        if (x <= 0) x = 0

        _walls(x)(z).Existe
    }
    def GetGeometry(x: Int, y: Int): Geometry = _walls(x)(y).Geometry
    def GetControl(x: Int, y: Int): RigidBodyControl = _walls(x)(y).Control

    for (x <- 0 until _cote ; y <- 0 until _cote)
        _walls(x)(y) = new Wall
}