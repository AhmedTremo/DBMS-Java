package ScrumMasters;

public class SQLTerm {
	String _strTableName;
	String _strColumnName;
	String _strOperator;
	Object _objValue;
	
	SQLTerm(String _strTableName,String _strColumnName,String _strOperator,Object _objValue){
		this ._strColumnName=_strColumnName;
		this._strTableName=_strTableName;
		this._strOperator=_strOperator;
		this._objValue=_objValue;
	}
	SQLTerm(){
		
	}
}
