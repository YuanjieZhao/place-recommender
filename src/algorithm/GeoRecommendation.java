package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.MySQLConnection;
import entity.Item;


// Recommendation based on geo distance and similar categories.
public class GeoRecommendation {
	
	// 1. get favorite restaurants of the user
	// 2. use the existing favorites, find out restaurants with similar catogories
	// 3. sort the list of restaurants based on the count of categories
	// 4. return a filtered list of restaurants that doesn't contain user's favorite restaurants	
	 public List<Item> recommendItems(String userId, double lat, double lon) {
			List<Item> recommendedItems = new ArrayList<>();
			
			MySQLConnection conn = new MySQLConnection();
			Set<String> favItemIds = conn.getFavoriteItemIds(userId);
			Map<String, Integer> categories = new HashMap<>();
			
			for (String itemId : favItemIds) {
				Set<String> allCategories = conn.getCategories(itemId);
				for (String category : allCategories) {
					categories.put(category, categories.getOrDefault(category, 0) + 1);
				}				
			}
			
			List<Entry<String, Integer>> entryList = new ArrayList<>(categories.entrySet());
			
			Collections.sort(entryList, (Entry<String, Integer> o1, Entry<String, Integer> o2) -> {
				return Integer.compare(o2.getValue(), o1.getValue());
			});
			
			// now I have a sorted list of category for recommendation. I need to get all restaurants that have these categories
			Set<Item> visitedItems = new HashSet<>();
			for (Entry<String, Integer> entry : entryList) {
				List<Item> items = conn.searchItems(lat, lon, entry.getKey());
				List<Item> filteredItems = new ArrayList<>();
				for (Item item : items) {
					if (!favItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
						filteredItems.add(item);
					}
				}
				
				visitedItems.addAll(filteredItems);
				recommendedItems.addAll(filteredItems);
			}
			
			
			return recommendedItems;
	 }
}
