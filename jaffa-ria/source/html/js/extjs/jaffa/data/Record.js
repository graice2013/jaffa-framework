/**
 * @class Jaffa.data.Record
 * @extends Ext.data.Record
 * @constructor
 * This constructor should not be used to create Record objects. Instead, use the constructor generated by
 * {@link #create}. The parameters are the same.
 * @param {Array} data An associative Array of data values keyed by the field name.
 * @param {Object} id (Optional) The id of the record. This id should be unique, and is used by the
 * {@link Ext.data.Store} object which owns the Record to index its collection of Records. If
 * not specified an integer id is generated.
 * @param {Object} o (Optional) The source object used to generate the data.
 */
Jaffa.data.Record = function(data, id, o){
    this.id = (id || id === 0) ? id : ++Ext.data.Record.AUTO_ID;
    this.data = data || {};
    if (this.fields) {
      // assign default values
      this.fields.each(function(f) {
        if (this.data[f.name] === undefined) {
          this.data[f.name] = f.defaultValue;
        }
      }, this);
    }
    //console.debug("Create JAFFA RECORD - id=",id, this);
    this.initData(data, id, o);
};

Ext.extend(Jaffa.data.Record, Ext.data.Record, {
    /**
     * @method
     * Initialize handler called on record construction.
     * This can be used to initialize calculated fields when this record is constructed
     */
    initData: function() {
        // console.debug("Initialize Record Data", this);
    }

  /**
   * @method
   * @access public
   * The sub stores are used in tree records to keep the arrays of items inside of a node. Assuming the name is the field name in the node that contains the array of items.
   * Common method that will be accessed by component.js. 
   * The creation of the store requires create[name]SubStore() to be implemented.
   * The sub store should have 'metaClass' property, which is the name of the graph represented 
   * by the records inside of the sub store.
   * @param {Object} name
   */
  ,getSubStore: function(name) {
    if (!name) return null;
    var upper1 = name.charAt(0).toUpperCase() + (name.length > 1 ? name.substr(1) : '');
    var creator = this['create' + upper1 + 'SubStore'];
    if (creator) {
      if (! this.subStores) {
        this.subStores = new Ext.util.MixedCollection();
      }
      var store = this.subStores.get(name);
      if (! store) {
        store = creator.call(this);
        this.subStores.add(name, store);
        // add dirty listeners
        store.on('add', function() {
          this.dirty = true;
        }, this);
        store.on('update', function() {
          this.dirty = true;
        }, this);
        store.on('remove', function() {
          this.dirty = true;
        }, this);
      }
      return store;
    }
    return null;
  },
  
  isSubStoreModified: function(){
    var isModified = false;
    if(this.subStores){
      this.subStores.each(function(store){
        if(store && store.modified && store.modified.length > 0){
          isModified = true;
        } else if(store && store.removed && store.removed.length > 0){
          isModified = true;
        }
      });
    }
    return isModified;
  }
  
  /**
   * @method
   * @access public
   * Returns an array of sub store names.
   */
  ,getSubStoreNames: function() {
    if (this.subStores) {
      var names = [];
      this.subStores.eachKey(function(k) {
        names.push(k);
      });
      return names;
    }
    return null;
  }
  
  /**
   * @method
   * Create the substores by copying the substores in the input record
   * @access public
   * @param {Jaffa.data.Record} rec
   *
   * @note method is not currently used; an inactive example is in DefaultStructurePanel.reorgHandler()
   */
  ,copySubStores: function(rec) {
    if (rec) {
      var subNames = rec.getSubStoreNames();
      if (subNames && subNames.length>0) {
        for (var i=0; i<subNames.length; i++) {
          var inputStore = rec.getSubStore(subNames[i]);
          var store = this.getSubStore(subNames[i]);
          // copy the records over
          if (store && inputStore.getCount()>0) {
            var inputRecData = [];
            var inputRecs = inputStore.getRange();
            for (var j=0; j<inputRecs.length; j++) {
              inputRecData.push(inputRecs[j].data);
            }
            store.loadData(inputRecData);
            // NOTE: the sub stores of the records in this substore is not copied.
          }
        }
      }
    }
  }
  
  /**
   * @method
   * @abstract
   * Returns an array of child objects.
   * This method should be implemented when using a TreeGridReader to build a Store for the tree widget.
   */
  ,getChildObjects: function () {
    return null;
  }
  
  /**
   * @method
   * Returns a JSON object constructed from this record according to the mappings of the fields.
   * This may be used for populating fields in a panel from a record with matching mapping values.
   * (not currently used as of April 19, 2010, noted by SeanZ)
   */
  ,convertToJSON: function() {
    var dd = {};
    this.fields.each(function(fld) {
      var mapping = fld.name;
      if (fld.mapping) mapping = fld.mapping;
      Jaffa.data.Util.set(dd, mapping, this.get(fld.name));
    }, this);
    return dd;
  }
});

/**
 * Generate a constructor for a specific record layout.
 * <p>
 * The key difference between this version an the base class, is the provision of the {@link #initData} method
 * <p>
 * @param {Array} o An Array of field definition objects which specify field names. For more details see {@link Ext.data.Record#create}
 * @method create
 * @return {function} A constructor which is used to create new Records according to the definition.
 * @static
 */
Jaffa.data.Record.create = function(o) {
    var f = Ext.extend(Jaffa.data.Record, {});
    var p = f.prototype;
    
    // hold field.name and Ext.data.Field pairs
    p.fields = new Ext.util.MixedCollection(false, function(field) {
        return field.name;
    });
    
    // hold field.mapping and field.name pairs
    p.mappingToName = new Ext.util.MixedCollection();
    
    // build the mixed collections
    for(var i = 0, len = o.length;i < len; i++) {
      var field = o[i];
      if(field){
        if(field.labelToken){
          field.label = Labels.get(field.labelToken);
        }
        var extField = new Ext.data.Field(field);
        if (extField.type && typeof extField.type === 'object')
          extField.type = extField.type.type;
        p.fields.add(extField);
        p.mappingToName.add(field.mapping ? field.mapping : field.name, field.name);
      }
    }
    
    /**
     * Returns the field object corresponding to the input field name.
     * @param {String} name A field name.
     * @return {Ext.data.Field} The corresponding field object.
     */
    f.getField = function (name) {
      return p.fields.get(name);
    };
    
    /**
     * Returns the value for the field in the current record that has the input mapping.
     * @param {String} name A mapping for a field.
     * @return {Object} The value for the field in the current record that has the input mapping.
     */
    p.getByMapping = function (mapping) {
      var name = this.mappingToName.get(mapping);
      return name ? this.get(name) : null;
    }
    
    return f;
};


