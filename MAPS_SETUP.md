# Melbourne Healthcare Finder with Interactive Map

CareConnect now features an **interactive map interface** specifically designed for Melbourne,
Australia using **OpenStreetMap and Overpass API** - completely free and without API key
restrictions!

## ✅ **What's Implemented:**

### 🗺️ **Map Interface**

- **Embedded map view** (300px height) within accordion
- **Interactive markers** filtered by selected type (All/Pharmacy/Clinic)
- **Full-screen map mode** with dedicated view
- **Real-time updates** when search parameters change
- **Coordinate display** showing current map center

### 🔍 **Search Capabilities**

- **Suburb name search** with Nominatim geocoding
- **Quick area buttons** for popular Melbourne suburbs
- **Custom coordinate input** for precise locations
- **Real-time filtering** of map markers and result list

### 🌐 **API Integration**

- **Overpass API**: Healthcare facility data from OpenStreetMap
- **Nominatim API**: Free geocoding for Melbourne suburbs
- **Retrofit**: Network layer for all API calls
- **No API keys required** - completely free solution

### 📱 **User Interface**

- **Embedded map** with full-screen option
- **Search bar** for Melbourne suburbs
- **Quick area buttons** for instant navigation
- **Filter chips** with live counts and map marker updates
- **Detailed place cards** with Australian healthcare context

### 🔧 **Technical Architecture**

- **MapsViewModel**: State management with geocoding and map centering
- **OverpassRepository**: Enhanced with Nominatim integration
- **Compose UI**: Reactive interface with map synchronization
- **Australian healthcare tag support** in OSM queries

## 🎯 **How to Use:**

### 🔍 **Search by Suburb**

1. Enter suburb name in search bar (e.g., "Carlton", "Fitzroy")
2. Tap "Search" to geocode and center map
3. View results on map and in list below

### 🏙️ **Quick Area Selection**

1. Tap area buttons (CBD, South Yarra, St Kilda, Richmond)
2. Map automatically centers on selected area
3. Healthcare search triggers automatically

### 📍 **Custom Coordinates**

1. Enter precise latitude/longitude coordinates
2. Tap "Search Healthcare Near Location"
3. View results on interactive map with markers

### 🗺️ **Map Interaction**

1. View embedded map showing current search area
2. Markers update based on filter selection (All/Pharmacy/Clinic)
3. Tap full-screen button for detailed map view
4. Use back button to return to embedded view

## 🇦🇺 **Melbourne-Specific Features:**

### 🏙️ **Quick Area Selection**
- **Melbourne CBD** (-37.8136, 144.9631)
- **South Yarra** (-37.8394, 144.9926)
- **St Kilda** (-37.8676, 144.9803)
- **Richmond** (-37.8197, 144.9917)

### 🌐 **Australian Healthcare Terms**

- **Pharmacies**: `amenity=pharmacy` and `shop=chemist` (Australian term)
- **Clinics**: `amenity=clinic`, `amenity=hospital`, `healthcare=clinic`, `healthcare=centre`
- **Medical Centers**: `healthcare=doctor`, `amenity=doctors`
- **Dentists**: `amenity=dentist`, `healthcare=dentist`

### 🔍 **Enhanced Search Parameters**
- **Pharmacy search**: 3km radius (suitable for Melbourne's suburban layout)
- **Clinic search**: 5km radius (accounts for Melbourne's medical precincts)
- **Comprehensive queries** including Australian-specific OSM tags

## 🌏 **Melbourne Coverage:**

- **Comprehensive suburb database** via Nominatim
- **Popular areas**: CBD, South Yarra, St Kilda, Richmond, Carlton, Fitzroy
- **All Melbourne suburbs** searchable by name
- **Accurate coordinates** for each area

## 🚀 **Technical Highlights:**

- **Reactive UI**: Map and list synchronize automatically
- **Free APIs**: No billing or API key restrictions
- **Australian focus**: Optimized for Melbourne's unique layout
- **Scalable**: Easy to extend to other Australian cities

## 📊 **Current Status:**

- ✅ **Embedded map interface** with OSMDroid integration
- ✅ **Suburb search** with Nominatim geocoding
- ✅ **Interactive filtering** with marker updates
- ✅ **Full-screen map** capability
- ✅ **Bottom navigation collision** fixed with proper padding
- ✅ **Error handling** with fallback views when OSMDroid fails
- ⚠️ **OSMDroid dependencies** (may need device-specific configuration)

## 🐛 **Troubleshooting:**

### Map Not Loading:

- **Fallback view shows**: Map will display placeholder with coordinates and marker counts
- **Permissions**: Ensure INTERNET, WRITE_EXTERNAL_STORAGE permissions are granted
- **Network**: OSMDroid requires internet connection to load map tiles
- **Device storage**: Map tiles are cached to device internal storage

### Bottom Navigation Collision:

- **Fixed**: Added 100dp bottom padding to prevent content overlap
- **Scrollable**: Content now properly scrolls above bottom navigation

### OSMDroid Issues:

- **Graceful degradation**: App works without functional map tiles
- **Error states**: Clear indicators when map initialization fails
- **Alternative**: Coordinates and place data still fully functional

## 🔮 **Future Enhancements:**

- **Live OSMDroid tiles** with interactive panning/zooming
- **Marker clustering** for dense areas
- **Route planning** to selected healthcare facilities
- **Offline map caching** for better performance
- **Tram/transport integration** for Melbourne-specific directions

The implementation provides a complete healthcare discovery experience with interactive mapping
specifically tailored for Melbourne, Australia!
