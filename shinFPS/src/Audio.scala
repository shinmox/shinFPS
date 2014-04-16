import com.jme3.asset.AssetManager
import com.jme3.audio.AudioNode
import com.jme3.scene.Node

/**
 * Created by shinmox on 14/04/14.
 */
class Audio (AssetManager: AssetManager, rootNode: Node) {
    private def _createAudio(chaine: String): AudioNode = {
        val audio_gun = new AudioNode(AssetManager, chaine, false)
        audio_gun.setLooping(false)
        audio_gun.setVolume(2)
        rootNode.attachChild(audio_gun)
        audio_gun
    }

    private val _gun = _createAudio("Sound/Effects/Gun.wav")
    private val _bang = _createAudio("Sound/Effects/Bang.wav")

    def PlayGun() {
        _gun.playInstance()
    }
    def PlayBang() {
        _bang.playInstance()
    }



}
