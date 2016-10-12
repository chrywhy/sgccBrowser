package com.chry.browser.bookmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookPageConstant
{
	// File Copy Cut Paste
	public static final String FILE_COPY_SEPARATOR = "#=sep=#";
	public static final int FILE_IO_BUFFER_SIZE = 4096;
	
	// FileSortType
	public static final String SORT_TYPE_VALUE = "name";
	public static final String SORT_TYPE_KEY = "名字";
	
	public static Map< String, Integer > getSortMap()
	{
		Map< String, Integer > sortMap = new HashMap< String, Integer >();
		Map< String, String > sortTypeMap = getSortTypeMap();
		List<String> sortTypeList = getSortTypeList();
		
		for( int i = 0; i < sortTypeList.size(); i++ )
		{
			int column = i + 1;
			String sortType = sortTypeMap.get( sortTypeList.get( i ) );
			sortMap.put( sortType, column );
		}
		
		return sortMap;
	}
	
	public static Map< String, String > getSortTypeMap()
	{
		return getSortTypeMap( SORT_TYPE_KEY, SORT_TYPE_VALUE );
	}
	
	public static Map< String, String > getSortTypeMap( String keys, String values )
	{
		String[] keyArray = keys.split( "," );
		String[] valueArray = values.split( "," );
		Map<String, String > sortTypeMap = new HashMap< String, String >();
		for( int i = 0 ; i < keyArray.length ; i ++ )
		{
			sortTypeMap.put( keyArray[i].trim(), valueArray[i].trim() );
		}
		return sortTypeMap;
	}
	
	public static List< String > getSortTypeList()
	{
		return getSortTypeList( SORT_TYPE_KEY );
	}
	
	public static List<String> getSortTypeList(String types)
	{
		String[] typeArray = types.split( ",");
		List< String > sortTypeList = new ArrayList< String >();
		for( String s : typeArray)
		{
			sortTypeList.add( s.trim() );
		}
		return sortTypeList;
	}
}
