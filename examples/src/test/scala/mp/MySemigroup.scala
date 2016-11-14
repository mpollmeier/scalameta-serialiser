package mp

class MySemigroup extends Semigroup[Int] {
  def append1(x: Int, y: Int): Int = x+y
}
