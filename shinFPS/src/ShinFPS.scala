
/**
 * Created by shinmox on 25/03/14.
 */
object ShinFPS
{
    /** main
      *
      * @param args non utilisé
      */
    def main(args: Array[String]) {
        new ShinFPS
    }
}

class ShinFPS {
    //TODO: Effacer les tirs lorsqu'une brique est retirée
    private val _modele = new Modele
    private val _ui = new Ui(_modele)
    _modele.AddObserver(_ui)
    _ui.start()
}
