The object_maintenance_2_0 pattern has the following features
=============================================================
- Uses Tiles to render the JSP
- Uses the object-maintenance-meta_2_0.dtd, which has the new attributes - MainLayout, MaintenanceLayout, ValidateFieldHandler, StampType, UseForDirtyReadCheck, Screens
- Added the wizard feature (previous/next/finish buttons)
- Added support for RELATED objects
- Supports the Delete constraints - restricted, cascading - for RELATED objects.
- Addressed the dirty read issue (Feature Request#727176) by introducing the 'UseForDirtyReadCheck' element in a field definition
- Added support for leave-field triggers through use of ValidateFieldHandler
- Added the StampType feature to stamp the following on a field - CreatedUserId, CreatedDateTime, LastUpdatedUserId, LastUpdatedDateTime
- Created a voucher generator interface and its default implementation. Allows a field to be labeled as voucher. The Tx will use the specified implementation to generate a voucher for the field
- Supports default values by using a properties file local to a component. Removed the <InitialValue> element
- Added the ability to specify 'DisplayOnly' fields on a component
- Created new base classes MaintComponent2, MaintAction, MaintForm
- The create and update methods now return the RetrieveOutDto
- Removed the code to put the indicator '*' next to mandatory fields. The indicator will now be displayed by the widget itself.
- Added the message 'label.Jaffa.Widgets.Button.Finish=Finish', 'label.Jaffa.Widgets.Button.Next=Next', 'label.Jaffa.Widgets.Button.Previous=Previous'
- Created the RelatedDomainObjectFoundException and the message 'exception.org.jaffa.exceptions.RelatedDomainObjectFoundException.restrictedConstraint=The related {0} object having 'Restricted Delete Constraint' was found. Delete cannot be performed.'

TODO for a future version of object_maintenance pattern
=======================================================
- Support the action 'Add New' in a maintenance screen. This will save the current changes and bring up a blank screen to add a new record
- Support cases where an object cannot be persisted unless the record(s) in a related component are entered
- Support AutoGenerated fields


Migrating from object_maintenance_1_0 to object_maintenance_2_0
===============================================================
- The pattern has been revamped and it is advisable not to perform a code-merge with the existing code line
- Before regenerating the maintenance components, get a list of customizations using SourceDecomposerUtils
- Add the customizations manually to the new files
- Enable the support for Tiles by adding the TilePlugin in struts-config.xml and using the tiles-defs.xml that comes bundled in jaffa-html.zip
- Check out the build script HttpUnitTestApp.xml on how to merge the tiles definition fragments at build time



Known Limitations
=================

For In-line one-to-one relationships only
-----------------------------------------
- Related objects don't support dirty-reads. 
  Even if your inner object supports a time stamp <StampType>LastUpdatedDateTime</StampType>,
  it will not support the use of the <UseForDirtyReadCheck>true</UseForDirtyReadCheck>.
  

- Related objects don't impact the time stamps of the parent record. 
  If you update a field on a related one-to-one object and save it, as you have not updated
  the primary object, the primary objects time stamp is not effected. You can have independent
  time stamps on the related object.
