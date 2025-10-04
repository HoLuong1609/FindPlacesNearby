# 🗺️ FindPlacesNearby

**FindPlacesNearby** is an Android application that uses the **Google Maps API** to help users discover nearby places, view details, get directions, and manage their favorite locations.  
It supports searching by name or category, viewing reviews, saving favorites, and managing search history — all through an interactive map interface.

---

## 🚀 Key Features

### 🔍 Search & Suggestions
- Search for places by **name** or **category**.  
- Display **auto-suggestions** while typing.  
- Show **search results** on the map and inside a **Sliding Drawer**.  
- Save and display **search history**.

### 📍 Map & Location
- Integrated **Google Maps Fragment**.  
- Move camera to **current user location**.  
- Display **markers** for all search results.  
- Update user position with `OnLocationChanged()`.  
- **Save and restore map state** between sessions.

### 🧭 Directions & Distance
- Get **driving or walking directions** between two points.  
- Calculate **distance and estimated travel time**.  
- Allow users to **swap origin and destination** to reverse routes.

### ⭐ Place Management
- View **detailed information** about a place in a **Sliding Drawer**.  
- Add **reviews** for places.  
- **Add or remove** places from the **favorites list**.  
- **Call** a place or open its **website** directly.  
- **Add new places** to the server.

### 🧩 Interface & Navigation
- **Navigation Drawer** with the following sections:  
  - 📜 **History** – View past search history.  
  - ⭐ **Bookmarks** – View favorite places.  
  - ⚙️ **Settings** – Adjust search radius for Google Maps.  
- **SlidingDrawerDirection** to show detailed place info.  
- **MyFragmentManager** to manage `SearchFragment` and `MapsFragment`.  
- Automatically **close SlidingDrawer** when opening the Search Fragment.  
- Handle `onBackPressed()` properly when drawers or fragments are open.

