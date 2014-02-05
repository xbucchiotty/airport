type Entity = {
  type Key
  def key: Key
}

case class Product(key: String) {
  type Key = String
}


trait Store[T <: Entity] {
  def apply(key: T#Key): Option[T]

  def +=(entity: T)
}

trait Repository[T <: Entity] {
  store: Store[T] =>

  def find(key: T#Key): Option[T] =
    store(key)

  def save(entity: T) {
    store += entity
  }
}

trait InMemoryStore[T <: Entity] extends Store[T] {
  private var data = Map.empty[T#Key, T]

  def apply(key: T#Key) = data.get(key)

  def +=(entity: T) {
    data += (entity.key -> entity)
  }
}

val debugRepo = new Repository[Product] with Store[Product] {
  def apply(key: String) = {
    println(s"==> get $key")
    None
  }

  def +=(entity: Product) {
    println(s"==> put $entity")
  }
}

val mapRepo = new Repository[Product] with InMemoryStore[Product]

def test(repo: Repository[Product]) {
  repo.save(Product("Test"))
  repo.find("Test")
  repo.find(1)
}









test(mapRepo)


test(debugRepo)




