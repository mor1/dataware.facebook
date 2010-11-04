package datasphere.dataware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DSUpdate {
	
	//-- namespace of dataware source
	private String source;
	
	//-- namespace of item, defaulting to "dataware:update"
	private String type;
	
	//-- namespaces of associated item categories (interfaces)
	private ArrayList< String > tags;
	
	//-- time that the update was generated by the dataware
	private long mtime;
	
	//-- WGS-84 coordinate attached to the update
	public class Coordinate {
		private double lat;
		private double lon;
		public Coordinate() {};
		public Coordinate( double lat, double lon ) {
			this.lat = lat;
			this.lon = lon;
		}
		public double getLat() { return lat; };
		public double getLon() { return lon; };
	}
	
	private Coordinate loc;
	
	//-- create / read / update / delete
	private String action;

	//-- a short textual summary of the update
	private String desc;

	//-- the total number of items represented by this item type
	private long total;

	//-- other data associated with the update, specific to its type
	private HashMap< String, String > meta;

	public DSUpdate( String source, String type, String action ) 
	throws IOException {
		setSource( source );
		setAction( action );
		setType( type );
	}
	
	public DSUpdate( String source, String type )
	throws IOException {
		setSource( source );
		setAction( "create" );
		setType( type );
	}

	public DSUpdate( JSONObject json )
	throws IOException, JSONException {
		fromJSON( json );
	}
	
	public String getSource() 				{ return this.source; }
	public String getType() 				{ return this.type; }
	public ArrayList< String > getTags() 	{ return this.tags; }
	public long getMtime() 					{ return this.mtime; }
	public Coordinate getLocation() 		{ return this.loc; }
	public String getAction() 				{ return this.action; }
	public String getDesc() 				{ return this.desc; }
	public long getTotal() 					{ return this.total; }
	public HashMap< String, String > getMetadata() 	{ return this.meta; }

	public DSUpdate setSource( String source ) 			{ this.source = source; return this; }
	public DSUpdate setType( String type ) 				{ this.type = type; return this; }
	public DSUpdate setTags( ArrayList< String > tags ) { this.tags = tags; return this; }
	public DSUpdate setMtime( long mtime ) 				{ this.mtime = mtime; return this; }
	public DSUpdate setLocation( Coordinate location ) 	{ this.loc = location; return this; }
	public DSUpdate setDesc( String desc ) 				{ this.desc = desc; return this; }
	public DSUpdate setTotal( long total ) 				{ this.total = total; return this; }
	
	public DSUpdate setLocation( double lat, double lon ) { 
		this.loc = new Coordinate( lat, lon );
		return this; 
	}
	
	public DSUpdate setAction( String action )
	throws IOException { 
		if ( action.equals( "create" ) || 
			 action.equals( "read" )   || 
			 action.equals( "update" ) || 
			 action.equals( "delete" ) ) {
			this.action = action; 
			return this;	
		}
		else {
			throw new IOException();
		}
	}

	public DSUpdate addTag( String tag ) {
		
		if ( tags == null ) 
			tags = new ArrayList< String >();
		
		this.tags.add( tag );
		return this;
	}

	public DSUpdate addMetadata( String key, String value ) {
		
		if ( meta == null ) 
			meta = new HashMap< String, String >();
		
		this.meta.put( key, value );
		return this;
	}


	public DSUpdate addMetadata( String key, int intValue) {
		return addMetadata( key, Integer.toString( intValue ) );		
	}
	
	@Override
	public String toString() {
		return toJSON();
	}


	public String toJSON() {
		Gson gson = new GsonBuilder().create();
		String result = gson.toJson( this );
		return result;
	}
		
	@SuppressWarnings("unchecked")
	public void fromJSON( JSONObject j ) 
	throws JSONException {
		
		//-- mandatory fields
		this.source = j.getString( "source" );
		this.type = j.getString( "type" );
		this.action = j.getString( "action" );
		this.mtime = j.getLong( "mtime" );
		
		//-- optional fields
		this.desc = j.has( "desc" ) ? j.getString( "desc" ) : "no description";
		this.total =  j.has( "total" ) ? j.getLong( "total" ) : 0;
		
		if ( j.has( "meta") ) {
			JSONArray jsonTags = j.getJSONArray( "tags" );
			this.tags = new ArrayList< String >();
			for ( int i = 0; i < jsonTags.length(); i++ ) 
				this.tags.add( jsonTags.getString( i ) );
		}
		
		
		if ( j.has( "meta") ) {
			JSONObject jsonMeta = j.getJSONObject( "meta" );
			this.meta = new HashMap< String, String >();
			Iterator ji = jsonMeta.keys();
			while ( ji.hasNext() ) {
				String key = ( String ) ji.next();
				String val = jsonMeta.getString( key );
				this.meta.put( key, val );
			}
		}
		
		if ( j.has( "loc") ) {
			JSONObject jsonLoc = j.getJSONObject( "loc" );
			this.loc = new Coordinate (
				jsonLoc.getLong( "lon" ),
				jsonLoc.getLong( "lat" ) 
			);
		}
	}
		
	
	public String toXML() {
		
		String locString =	( loc == null ) ? "" :
			"<loc>" +
				"<lon>" + loc.getLon() + "</lon>" +
				"<lat>" + loc.getLat() + "</lat>" +
			"</loc>";
		
		String metaXML = "";
		if ( meta != null) {
			metaXML = "<meta>";
			for ( Entry< String, String > e : meta.entrySet() ) { 
				metaXML += 
					"<" + e.getKey() + ">" +
					e.getValue() + 
					"</" + e.getKey() + ">";
			}
			metaXML += "</meta>";
		}		
		
		String tagsXML = "";
		if ( tags != null) 
			for ( String e : tags ) tagsXML += "<tag>" + e + "</tag>";

		
		String s = 
			"<DSUpdate>" + 
				"<source>" + source + "</source>" +
				"<type>" + type + "</type>" +
				"<desc>" + desc + "</desc>" +
				"<action>" + action + "</action>" +
				"<mtime>" + mtime + "</mtime>" +
				"<total>" + total + "</total>" +
				locString +
				tagsXML + 
				metaXML +
			"</DSUpdate>";

		return s;
	}

}