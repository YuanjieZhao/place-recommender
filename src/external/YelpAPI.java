package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class YelpAPI{
	private static final String HOST = "https://api.yelp.com";
	private static final String ENDPOINT = "/v3/businesses/search";
	private static final String DEFAULT_TERM = "";
	
//	Number of business results to return. By default, it will return 20. Maximum is 50
//  https://www.yelp.com/developers/documentation/v3/business_search	
	private static final int SEARCH_LIMIT = 20;  

	private static final String TOKEN_TYPE = "Bearer";
	private static final String API_KEY = "jYIEkk_PQ7V4MB7Y73ueHQNi5qhKNWDR48WIqj_LnHpUBvP_yMFstWxFhTYcyT6RMFNPcwFWmFaGBbrWIWIdcs7j8jSzkdQVdY6Wur8_mf0_v2vypyu_pNOFjMsfXHYx";

	// send a GET request to Yelp server to request list of restaurants given latitude and longitude. 
	// return null if response code != 200 or JSON response doesn't contain valid "businesses" field
 	public List<Item> search(double lat, double lon, String term) {
 		String latitude = lat + "";
 		String longitude = lon + "";
 		
 		try {
 			if (term == null || term.isEmpty()) {
 				term = DEFAULT_TERM;
 			} else {
 				term = java.net.URLEncoder.encode(term, "UTF-8");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s", term, lat, lon, SEARCH_LIMIT);
 		String url = HOST + ENDPOINT + "?" + query;
 		
 		try {
 			System.out.println(url);
 			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
 			connection.setRequestMethod("GET");
 			connection.setRequestProperty("Authorization", TOKEN_TYPE + " " + API_KEY);
 			
 			int responseCode = connection.getResponseCode();
 			System.out.println("response code: " + responseCode);
 			
 			if (responseCode != 200) {
 				return new ArrayList<>();
 			}
 			
 			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			StringBuilder str = new StringBuilder();
 			String inputLine = "";

 			while((inputLine = in.readLine())!= null) {
 				str.append(inputLine);
 			}
 			
 			in.close();
 			
 			JSONObject obj = new JSONObject(str.toString());
 			if (!obj.isNull("businesses")) {
 				return getItemList(obj.getJSONArray("businesses"));
 			}
 		
 		} catch (Exception e) {
 			e.printStackTrace();
 		}

		return null;
	}
 	
	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray restaurants) throws JSONException {
		List<Item> list = new ArrayList<>();
		
		for (int i = 0; i < restaurants.length(); ++i) {
			JSONObject restaurant = restaurants.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			if(!restaurant.isNull("id")) {
				builder.setItemId(restaurant.getString("id"));
			}
			if(!restaurant.isNull("name")) {
				builder.setName(restaurant.getString("name"));
			}
			if(!restaurant.isNull("url")) {
				builder.setUrl(restaurant.getString("url"));
			}
			if(!restaurant.isNull("image_url")) {
				builder.setImageUrl(restaurant.getString("image_url"));
			}
			if(!restaurant.isNull("rating")) {
				builder.setRating(restaurant.getDouble("rating"));
			}
			if(!restaurant.isNull("distance")) {
				builder.setDistance(restaurant.getDouble("distance"));
			}
			if(!restaurant.isNull("categories")) {
				builder.setCategories(getCategories(restaurant));
			}
			if(!restaurant.isNull("location")) {
				JSONObject location = (JSONObject) restaurant.get("location");
				builder.setAddress(getAddress(location));
			}			
			
			list.add(builder.build());
		}

		return list;
	}
	
	private Set<String> getCategories(JSONObject restaurant) throws JSONException {
		Set<String> categories = new HashSet<>();
		
		JSONArray jsonArray = (JSONArray) restaurant.get("categories");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject category  = jsonArray.getJSONObject(i);
			if (!category.isNull("alias")) {
				categories.add(category.getString("alias"));
			}
		}

		return categories;
	}

	private String getAddress(JSONObject location) throws JSONException {
		String fullAddress = "";
		
		if(!location.isNull("display_address")) {
			if (!location.isNull("display_address")) {
				JSONArray array = location.getJSONArray("display_address");
				fullAddress = array.join(",");
			}
		}
		
		return fullAddress;
	}


 	
 	// 	used for debugging 
	private void queryAPI(double lat, double lon) {
		List<Item> items = search(lat, lon, null);
		try {
		    for (Item item : items) {
		        JSONObject jsonObject = item.toJSONObject();
		        System.out.println(jsonObject);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Main entry for sample Yelp API requests.
	 */
	public static void main(String[] args) {
		YelpAPI tmApi = new YelpAPI();
		tmApi.queryAPI(37.38, -122.08);
	}


}
