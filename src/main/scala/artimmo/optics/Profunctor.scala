package artimmo.optics

trait Profunctor[F[_, _]] {
  def dimap[A, B, C, D](fab: F[A, B], ac: C => A, bd: B => D): F[C, D]
}

trait Cart[F[_, _] : Profunctor] {
  def first[A, B, C](fab: F[A, B]): F[(A, C), (B, C)]
}
