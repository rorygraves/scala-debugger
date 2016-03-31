package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ValueInfoProfile, VariableInfoProfile}

import scala.util.Try

/**
 * Represents a pure implementation of a field profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param objectReference The object associated with the field instance
 * @param field The reference to the underlying JDI field
 * @param virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class PureFieldInfoProfile(
  private val objectReference: ObjectReference,
  private val field: Field
)(
  private val virtualMachine: VirtualMachine = field.virtualMachine()
) extends VariableInfoProfile {

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Field = field

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = field.name()

  /**
   * Returns whether or not this variable represents a field.
   *
   * @return True if a field, otherwise false
   */
  override def isField: Boolean = true

  /**
   * Returns whether or not this variable represents an argument.
   *
   * @return True if an argument, otherwise false
   */
  override def isArgument: Boolean = false

  /**
   * Returns whether or not this variable represents a local variable.
   *
   * @return True if a local variable, otherwise false
   */
  override def isLocal: Boolean = false

  /**
   * Sets the primitive value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  override def setValue(value: AnyVal): AnyVal = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    val mirrorValue = virtualMachine.mirrorOf(value)
    setFieldValue(mirrorValue)
    value
  }

  /**
   * Sets the string value of this variable.
   *
   * @param value The new value for the variable
   * @return The new value
   */
  override def setValue(value: String): String = {
    val mirrorValue = virtualMachine.mirrorOf(value)
    setFieldValue(mirrorValue)
    value
  }

  private def setFieldValue(value: Value): Unit =
    objectReference.setValue(field, value)

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  override def toValue: ValueInfoProfile = newValueProfile(
    objectReference.getValue(field)
  )

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(value)
}
