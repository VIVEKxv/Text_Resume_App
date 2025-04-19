// Resume.kt

data class Resume(
    val name: String,
    val phone: String,
    val email: String,
    val twitter: String,
    val address: String,
    val summary: String,
    val skills: List<String>,
    val projects: List<Project>  
)

data class Project(
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String
)
