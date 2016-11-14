package mp

@typeclass trait Semigroup[A] {
  @op("|+|") def append1(x: A, y: A): A
}
