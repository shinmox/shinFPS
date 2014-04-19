import scala.collection.mutable

/**
 * Created by shinmox on 03/04/14.
 */

/** Classe de décision de l'action des minions */
class IA(_personnages: mutable.Map[String, Personnage], _configuration: Configuration) {
    //TODO: Posséder une vision de l'AirDeJeu qui se construit au fur et à mesure de l'exploration par les minions
    //TODO: Donner des ordres à chacun des minions à chaque tour

    private val _cote = _configuration.Cote
    private val _visionDuJeu = Array.ofDim[Case](_cote, _cote)
    private var positionCoeur: (Int, Int) = null
    private var positionJoueur: (Int, Int) = null
    private var _jeuEnCours: Boolean = true

    def SimpleUpdate() {
        def RecupereVision(minion: Personnage) {
            val vision: Vision = minion.Regarde()
            for (x <- 0 until _cote; z <- 0 until _cote) {
                if (_visionDuJeu(x)(z) != null && _visionDuJeu(x)(z).Joueur) {
                    _visionDuJeu(x)(z).Joueur = false
                }
                if (_visionDuJeu(x)(z) != null && _visionDuJeu(x)(z).CoeurDuDonjon) {
                    _visionDuJeu(x)(z).CoeurDuDonjon = false
                }
                if (vision.Cases(x)(z) != null) {
                    _visionDuJeu(x)(z) = vision.Cases(x)(z)
                }
            }
        }
        def UpdateVision() {
            for (personnage <- _personnages.values
                if personnage.isInstanceOf[Minion]
                if !personnage.EnCoursAction ) {
                    RecupereVision(personnage)
                    println("IA.UpdateVision : J'ai recup la vision")
            }
        }
        def ChercheENI() {
            positionCoeur = null
            positionJoueur = null
            for(x <- 0 until _visionDuJeu.length ; z <- 0 until _visionDuJeu.length)
                if (_visionDuJeu(x)(z) != null) {
                    if (_visionDuJeu(x)(z).CoeurDuDonjon)
                        positionCoeur = (x, z)
                    if (_visionDuJeu(x)(z).Joueur)
                        positionJoueur = (x, z)
                }
        }
        def Attaque() {
            if (positionCoeur != null)
                for (personnage <- _personnages.values
                     if _jeuEnCours
                     if personnage.isInstanceOf[Minion] ) {
                        personnage.Attaque(_configuration.CoeurName, positionCoeur)
                     }
            else if (positionJoueur != null)
                for (personnage <- _personnages.values
                     if _jeuEnCours
                     if personnage.isInstanceOf[Minion] ) {
                        personnage.Attaque(_configuration.PlayerName, positionJoueur)
                }
            else
                for (personnage <- _personnages.values
                     if _jeuEnCours
                     if personnage.isInstanceOf[Minion] ) {
                        personnage.DeplaceAleatoirement()
                }
        }

        for (i <- 0 until _visionDuJeu.length ; j <- 0 until _visionDuJeu.length) {
            if (_visionDuJeu(i)(j) != null && _visionDuJeu(i)(j).Joueur) {
                println("je te vois toujours ^^ en " + i + " " + j)
            }
        }

        UpdateVision()
        ChercheENI()
        Attaque()

    }
    def CoeurDetruit() {
        _jeuEnCours = false
    }
}
