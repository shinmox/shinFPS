/**
 * Created by shinmox on 03/04/14.
 */

/** Classe initialisant l'air de jeu
  * ... bien que prévu au départ pour la gérer ... */
class AirDeJeu(_configuration: Configuration) {
    private val cote = _configuration.Cote
    val Table = Array.ofDim[Case](cote, cote)
    for (i <- 0 until cote ; j <- 0 until cote) {
        val Case = new Case
        Case.Mur = true
        Table(i)(j) = Case
    }

    private val _demiCote: Int = if ((0.5f*cote )% 2 == 0)(0.5f*cote).toInt
                                 else (0.5f*cote + 1).toInt

    var PositionApparitionMinions : (Int, Float, Int) = (cote -1, _configuration.HauteurSol, _demiCote-1)
    var PositionCoeurDonjon:        (Int, Float, Int) = (1,       _configuration.HauteurSol, _demiCote-1)

    private def RemoveZones() {
        //TODO : Trouver la version fonctionnelle
        def RemoveEniZoneBlocks() {
            // On retire une zone de 2*3 blocks comme zone d'apparition des mobs (ennemis)
            Table(PositionApparitionMinions._1   )(PositionApparitionMinions._3 -1).Mur = false
            Table(PositionApparitionMinions._1   )(PositionApparitionMinions._3   ).Mur = false
            Table(PositionApparitionMinions._1   )(PositionApparitionMinions._3 +1).Mur = false

            Table(PositionApparitionMinions._1 -1)(PositionApparitionMinions._3 -1).Mur = false
            Table(PositionApparitionMinions._1 -1)(PositionApparitionMinions._3   ).Mur = false
            Table(PositionApparitionMinions._1 -1)(PositionApparitionMinions._3 +1).Mur = false
        }
        def RemoveCoeurZoneBlocks() {
            //TODO: Retirer les blocks en fonction d'un point défini
            // On retire une zone de 3*3 blocks comme zone de cible ennemi
            Table(PositionCoeurDonjon._1 -1)(PositionApparitionMinions._3 -1).Mur = false
            Table(PositionCoeurDonjon._1   )(PositionApparitionMinions._3 -1).Mur = false
            Table(PositionCoeurDonjon._1 +1)(PositionApparitionMinions._3 -1).Mur = false
            Table(PositionCoeurDonjon._1 -1)(PositionApparitionMinions._3   ).Mur = false
            Table(PositionCoeurDonjon._1   )(PositionApparitionMinions._3   ).Mur = false
            Table(PositionCoeurDonjon._1 +1)(PositionApparitionMinions._3   ).Mur = false
            Table(PositionCoeurDonjon._1 -1)(PositionApparitionMinions._3 +1).Mur = false
            Table(PositionCoeurDonjon._1   )(PositionApparitionMinions._3 +1).Mur = false
            Table(PositionCoeurDonjon._1 +1)(PositionApparitionMinions._3 +1).Mur = false
        }

        RemoveEniZoneBlocks()
        RemoveCoeurZoneBlocks()
    }
    private def CreateDonjon() {
        Table(PositionCoeurDonjon._1)(PositionCoeurDonjon._3).CoeurDuDonjon = true
        Table(PositionCoeurDonjon._1)(PositionCoeurDonjon._3).Mur = false
    }

    RemoveZones()
    CreateDonjon()

    def RemoveMur(x: Int, z: Int) {
        Table(x)(z).Mur = false
    }

    // HELPER
    //    private def _printTable() {
    //        for (x <- (0 until  cote).reverse) {
    //            for (z <- 0 until cote) {
    //                if (Table(x)(z).Mur)
    //                    print(1 + " ")
    //                else
    //                    print(0 + " ")
    //            }
    //            print("\n")
    //        }
    //        print("\n")
    //    }
}
