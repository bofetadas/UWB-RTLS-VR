package de.MaxBauer.UWBRTLSVR

data class AccelerationData(val xAcc: Double, val yAcc: Double, val zAcc: Double) {

    override fun toString(): String {
        val xAcc = StringUtil.inEuropeanNotation(xAcc)
        val yAcc = StringUtil.inEuropeanNotation(yAcc)
        val zAcc = StringUtil.inEuropeanNotation(zAcc)
        return "$xAcc, $yAcc, $zAcc"
    }
}
