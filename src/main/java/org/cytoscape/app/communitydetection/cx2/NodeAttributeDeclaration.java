package org.cytoscape.app.communitydetection.cx2;

/**
 *
 * @author churas
 */
public class NodeAttributeDeclaration {
	
	private String _attributeName;
	private String _dataType;
	private String _alias;
	private Object _defaultValue;
	
	public NodeAttributeDeclaration(final String attributeName,
			final String dataType, final String alias,
			final Object defaultValue){
		_attributeName = attributeName;
		_dataType = dataType;
		_alias = alias;
		_defaultValue = defaultValue;
	}

	public String getAttributeName() {
		return _attributeName;
	}

	public void setAttributeName(String _attributeName) {
		this._attributeName = _attributeName;
	}

	public String getDataType() {
		return _dataType;
	}

	public void setDataType(String _dataType) {
		this._dataType = _dataType;
	}

	public String getAlias() {
		return _alias;
	}

	public void setAlias(String _alias) {
		this._alias = _alias;
	}

	public Object getDefaultValue() {
		return _defaultValue;
	}

	public void setDefaultValue(Object _defaultValue) {
		this._defaultValue = _defaultValue;
	}
	
	
}
