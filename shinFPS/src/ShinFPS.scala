
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
    //TODO: Mettre en place des créations d'obstacle
    //TODO: Tir dans les murs exterieurs
    //TODO: Effacer les tirs lorsqu'une brique est retirée
    //TODO: Régler la lumière
    //TODO: IA permettant d'aller au centre pour détruire le donjon

    //TODO: Patron MVC
    private val _modele = new Modele
    private val _ui = new Ui(_modele)
    _modele.AddObserver(_ui)
    _modele.Start()
}
