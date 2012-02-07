import org.squeryl.annotations.Transient
import org.squeryl.dsl.CompositeKey2
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{KeyedEntity, Schema}

/**
 * Test schema and entities.
 * User: roka
 * Date: 04.02.12
 * Time: 22:50
 */

case class Address(id: Long, street: String) extends KeyedEntity[Long]

case class Phone(id: Long, number: String) extends KeyedEntity[Long]

/**
 * Adds relations and a merge method to the entity.
 */
trait ContactRelations {
  self:Contact =>

  @Transient
  var primaryAddress: Option[Address] = _
  @Transient
  var addresses: List[Address] = Nil
  @Transient
  var phoneNumbers: List[Phone] = Nil

  def withRelations(primaryAddress: Option[Address], addresses: List[Address], phoneNumbers: List[Phone]) = {
    this.primaryAddress = primaryAddress
    this.addresses = addresses
    this.phoneNumbers = phoneNumbers
    this
  }
}

case class Contact(id: Long, name: String, primaryAddressId: Option[Long]) extends KeyedEntity[Long] with ContactRelations {
  def this() = this(-1, "", Some(-1))
}


// relation classes
class ContactAddress(val contactId: Long,
                     val addressId: Long,
                     val isPrimary: Boolean) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(contactId, addressId)
}

class ContactPhone(val contactId: Long,
                   val phoneId: Long,
                   val isPrimary: Boolean) extends KeyedEntity[CompositeKey2[Long, Long]] {
  def id = compositeKey(contactId, phoneId)
}

object TestSchema extends Schema {
  val addresses = table[Address]
  val contacts = table[Contact]
  val phones = table[Phone]

  // RELATION TABLES
  val contactAddresses = table[ContactAddress]
  val contactPhones = table[ContactPhone]
}
