# 🌱 Plant & Disease Detection Module - Quick Start Guide

## Welcome to the Plant Management System!

This module allows you to:
- 📝 Manage your plants (add, edit, delete)
- 📷 Capture plant images with your camera
- 🤖 Detect diseases using AI
- 💊 Track treatments and recommendations
- 📊 Monitor plant health over time

---

## 🚀 Getting Started

### 1. Access the Plant Module
1. Open AgriTrack app
2. From the dashboard (Accueil), tap the **"Plantes"** card
3. You'll see the Plant Management screen

### 2. Add Your First Plant
1. Tap the **+ (FAB)** button at the bottom right
2. Fill in the plant details:
   - **Nom** (Name): e.g., "Tomate Cherry"
   - **Type**: Select from Céréale, Fruit, or Légume
   - **Stade de croissance**: Current growth stage
   - **Emplacement**: Where the plant is located
   - **Quantité**: Number of plants
   - **Surface**: Area in m² or hectares
   - **Notes**: Any additional information
   - **Date de plantation**: When you planted it
3. Tap **"Ajouter"**
4. Your plant is now saved!

### 3. Detect Plant Diseases
1. From the Plant list, tap the **📷 camera icon** on any plant
2. OR tap the plant name to open PlantTreatment screen
3. Select your plant from the dropdown (if not already selected)
4. Tap **"📷 Capturer Image"**
5. Grant camera permission if asked
6. Take a clear photo of the plant (focus on leaves/affected areas)
7. Tap **"🔍 Analyser avec IA"**
8. Wait a moment for AI analysis
9. View the results:
   - **Disease name** (e.g., "Mildiou", "Oïdium")
   - **Confidence score** (how sure the AI is)
   - **Severity level** (Faible, Modéré, Sévère)
   - **Recommended action** (what to do next)
10. The treatment is automatically saved!

### 4. View Treatment History
- All detected diseases and treatments are listed below the camera section
- Each treatment shows:
  - Image thumbnail
  - Disease name and confidence
  - Detection date
  - Current status
- Tap **Edit** to update treatment status
- Tap **Delete** to remove a treatment record

---

## 📋 Features Overview

### Plant Management
- ✅ Add unlimited plants
- ✅ Edit plant details anytime
- ✅ Delete plants (treatments are also deleted)
- ✅ Filter by type or growth stage
- ✅ Track planting and harvest dates
- ✅ Monitor growth stages

### Disease Detection
- ✅ Camera integration
- ✅ AI-powered analysis
- ✅ 8 common diseases detected:
  - Aucune maladie (Healthy)
  - Mildiou (Downy Mildew)
  - Oïdium (Powdery Mildew)
  - Rouille (Rust)
  - Tache noire (Black Spot)
  - Pourriture (Rot)
  - Fusariose (Fusarium)
  - Anthracnose
- ✅ Confidence scoring
- ✅ Severity assessment
- ✅ Treatment recommendations

### Treatment Tracking
- ✅ Automatic record creation
- ✅ Image storage
- ✅ Status updates (Détecté → En traitement → Traité)
- ✅ Treatment history per plant
- ✅ Edit and delete capabilities

---

## 💡 Tips for Best Results

### Taking Photos
1. **Good lighting:** Take photos in natural daylight
2. **Focus on affected areas:** Get close to diseased leaves
3. **Clear images:** Avoid blurry photos
4. **Multiple angles:** Take several photos if unsure
5. **Clean lens:** Make sure your camera lens is clean

### Managing Plants
1. **Regular updates:** Update growth stage as plants develop
2. **Accurate dates:** Record planting dates for better tracking
3. **Detailed notes:** Add observations about plant health
4. **Location tracking:** Specify field/zone for organization

### Treatment Management
1. **Update status:** Mark treatments as "En traitement" when you apply them
2. **Mark complete:** Change to "Traité" when resolved
3. **Keep records:** Don't delete old treatments - they're useful for history
4. **Follow recommendations:** The AI provides specific treatment advice

---

## 🔧 Troubleshooting

### Camera Not Working
- **Check permissions:** Go to Settings → Apps → AgriTrack → Permissions → Enable Camera
- **Restart app:** Close and reopen the app
- **Device compatibility:** Ensure your device has a working camera

### AI Analysis Fails
- **Check image:** Make sure the photo was captured successfully
- **Select plant:** Ensure you've selected a plant from the dropdown
- **Try again:** Tap "Analyser avec IA" again
- **Check internet:** Some AI models may require internet (future versions)

### Plant Not Saving
- **Required fields:** Make sure you've filled in the plant name
- **Database error:** Check if you have storage space
- **Restart app:** Try closing and reopening the app

### Treatments Not Showing
- **Select plant:** Make sure you've selected the correct plant
- **Refresh:** Go back and return to the screen
- **Check database:** Ensure treatments were saved (look for success message)

---

## 📱 Permissions Required

### Camera Permission
- **Why:** To capture plant images for disease detection
- **When:** Asked when you first tap "Capturer Image"
- **How to grant:** Tap "Allow" when prompted

### Storage Permission
- **Why:** To save captured images
- **When:** Automatically handled on modern Android versions
- **How to grant:** Usually granted automatically

---

## 🎯 Common Use Cases

### Scenario 1: New Plant
1. Add plant with details
2. Set growth stage to "Semis"
3. Update stage as it grows
4. Monitor for diseases regularly

### Scenario 2: Disease Detected
1. Notice unhealthy plant
2. Open PlantTreatment
3. Capture image of affected area
4. Analyze with AI
5. Follow recommended treatment
6. Update status as you treat
7. Mark as "Traité" when resolved

### Scenario 3: Harvest Planning
1. View all plants
2. Filter by growth stage "Floraison"
3. Check harvest dates
4. Plan harvesting schedule

### Scenario 4: Treatment History
1. Select a plant
2. View all past treatments
3. Identify recurring diseases
4. Adjust prevention strategies

---

## 📊 Understanding Results

### Confidence Score
- **85-100%:** Very confident - likely accurate
- **70-84%:** Confident - probably correct
- **50-69%:** Moderate - consider other factors
- **Below 50%:** Low confidence - verify manually

### Severity Levels
- **Sévère:** Immediate action required
- **Modéré:** Monitor and treat soon
- **Faible:** Minor issue, preventive measures

### Status Meanings
- **Détecté:** Disease just identified
- **En traitement:** Currently applying treatment
- **Traité:** Treatment completed, resolved

---

## 🌟 Best Practices

1. **Regular monitoring:** Check plants weekly
2. **Early detection:** Catch diseases early for better outcomes
3. **Keep records:** Maintain treatment history
4. **Follow recommendations:** AI provides specific advice
5. **Update status:** Keep treatment status current
6. **Document everything:** Use notes field for observations

---

## 📞 Need Help?

### Documentation
- **Technical details:** See `PLANT_MODULE_DOCUMENTATION.md`
- **Implementation:** See `IMPLEMENTATION_SUMMARY.md`
- **Code:** Check inline comments in source files

### Support
- Check the troubleshooting section above
- Review the tips for best results
- Ensure all permissions are granted
- Try restarting the app

---

## 🎉 You're Ready!

Start managing your plants and detecting diseases with AI-powered precision!

**Happy Farming! 🌾🚜**

