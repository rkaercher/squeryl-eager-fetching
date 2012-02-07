import org.squeryl.adapters.MySQLAdapter
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{Session, SessionFactory}

/**
 * Initiates session and creates schema. Requires a mysql db with schema, user and password all set to "squeryltest".
 * User: roka
 * Date: 04.02.12
 * Time: 22:53
 */

class TestStarter {

  def init {
    //create db session factory
    Class.forName("com.mysql.jdbc.Driver")
    SessionFactory.concreteFactory = Some(() => {
      val connString = "jdbc:mysql://%s:%d/%s?user=%s&password=%s".
        format("localhost", 3306, "squeryltest", "squeryltest", "squeryltest")
      val session = new Session(
        java.sql.DriverManager.getConnection(connString),
        new MySQLAdapter
      )
      session.setLogger(println(_))
      session
    })

    SessionFactory.newSession.bindToCurrentThread

    inTransaction {
      TestSchema.drop
      TestSchema.create
    }

    createTestData
    createRelations
  }

  def createTestData {
    val addr1 = TestSchema.addresses.insert(new Address(-1, "address1Street"))
    val addr2 = TestSchema.addresses.insert(new Address(-1, "address2Street"))
    val addr3 = TestSchema.addresses.insert(new Address(-1, "address3Street"))

    val contact1 = new Contact(-1, "contact1Name", Some(addr2.id))
    TestSchema.contacts.insert(contact1)
    val contact2 = new Contact(-1, "contact2Name", None)
    TestSchema.contacts.insert(contact2)

    val phone1 = new Phone(-1, "number one")
    TestSchema.phones.insert(phone1)
    val phone2 = new Phone(-1, "number two")
    TestSchema.phones.insert(phone2)
  }

  def createRelations {
    TestSchema.contactAddresses.insert(new ContactAddress(1, 1, false))
    TestSchema.contactAddresses.insert(new ContactAddress(1, 3, false))

    TestSchema.contactPhones.insert(new ContactPhone(1, 1, false))
    TestSchema.contactPhones.insert(new ContactPhone(1, 2, true))
  }

  // test method only
  def mapRelations(in: Iterable[(Contact, Option[Address], Option[Phone])]) = in.groupBy(x1 => x1._1).mapValues(x2 =>
    (x2.map(_._2.getOrElse(None)).toList.distinct,
      x2.map(_._3.getOrElse(None)).toList.distinct))

  // test method only
  def mapRelationsWithPrimary(in: Iterable[(Contact, Option[Address], Option[Phone], Option[Address])]) = in.groupBy(x1 => x1._1).mapValues(fullRow =>
    (fullRow.map(_._2.getOrElse(None)).toList.distinct,
      fullRow.map(_._3.getOrElse(None)).toList.distinct,
      fullRow.head._4))

  def mergeRelations(in: Iterable[(Contact, Option[Address], Option[Phone], Option[Address])]) = for ((contact, fullRow) <- in.groupBy(x1 => x1._1)) yield
    contact.withRelations(fullRow.head._2,
      if (fullRow.head._4.isDefined) fullRow.map(_._4.get).toList.distinct else Nil,
      if (fullRow.head._3.isDefined) fullRow.map(_._3.get).toList.distinct else Nil)


  def test {
    //retrieves all contacts with their relations and merges them
    //joins are the following:
    // 1. link table contacts->addresses
    // 2. addresses (via 1.)
    // 3. link table contacts->phonenumbers
    // 4. phonenumbers (via 3.)
    // 5. primary address
    val myTree = join(from(TestSchema.contacts)(c => select(c)),
      TestSchema.contactAddresses.leftOuter,
      TestSchema.addresses.leftOuter,
      TestSchema.contactPhones.leftOuter,
      TestSchema.phones.leftOuter,
      TestSchema.addresses.leftOuter)(
      (c, ca, a, cp, p, pa) =>
        select(c, a, p, pa)
          on(ca.map(_.contactId) === c.id,
          a.map(_.id) === ca.map(_.addressId),
          cp.map(_.contactId) === c.id,
          p.map(_.id) === cp.map(_.phoneId),
          pa.map(_.id) === c.primaryAddressId)
    ).toList
    val contacts = mergeRelations(myTree)
  }
}
