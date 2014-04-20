/**
 * Created by shinmox on 07/04/14.
 */
class Vision(_cote: Int) {
    val Cases = Array.ofDim[Case](_cote, _cote)
    for (x <- 0 until _cote ; y <- 0 until _cote) {
        Cases(x)(y) = null
    }

    // HELPER
    def _printTable() {

        print("X  ")
        for (z <- 0 until _cote) {
            print(z.toString)
            if (z < 9) print("  ") else print(" ")
        }
        print(" => Z \n")
        for (x <- (0 until  _cote).reverse) {
            print(x.toString)
            if (x < 10) print("  ") else print(" ")
            for (z <- 0 until _cote) {
                if (Cases(x)(z) == null)
                    print("?")
                else if (Cases(x)(z).Mur)
                    print("X")
                else if (Cases(x)(z).CoeurDuDonjon)
                    print("C")
                else if (Cases(x)(z).Minion)
                    print("*")
                else if (Cases(x)(z).Joueur)
                    print("$")
                else if (Cases(x)(z).Rien)
                    print(" ")
                print("  ")
            }
            print("\n")
        }
        println("")
    }
}
