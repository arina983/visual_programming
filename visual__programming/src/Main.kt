import kotlin.random.Random

class Human{
    var name: String = ""
        get() = field
        set(value) {
            field = value
        }
    var surname: String = ""
        get() = field
        set(value) {
            field = value
        }
    var second_name: String = ""
        get() = field
        set(value) {
            field = value
        }
    var x: Int = 0
        get() {
            return field
        }
        set(value) {
            field = value
        }
    var y: Int = 0
        get() {
            return field
        }
        set(value) {
            field = value
        }
    var current_speed: Int = 7
        get() {
            return field
        }
        set(value) {
            if (value in 0..16){
                field = value
            } else {
                println("ошибка, скорость $value невозможна для человека")
            }
        }
    var age: Int = 0
        get() {
            return field
        }
        set(value) {
            if (value in 0..100) {
                field = value
            } else {
                println("введенный возраст $value некорректен")
            }
        }

    constructor(_name: String, _surname: String, _second: String, _cs: Int, _age: Int){
        name = _name
        surname= _surname
        second_name = _second
        current_speed = _cs
        age = _age
        println("We created the human object with name and age: $surname $name $second_name, age: $age")
    }

    fun move(){
        val dx = Random.nextInt(-current_speed, current_speed + 1)
        val dy = Random.nextInt(-current_speed, current_speed + 1)

        x += dx
        y += dy

        println("$name (speed: $current_speed) moved by: ($dx, $dy) to: ($x, $y)")
    }
}
fun main(){

    val humans = arrayOf(
        Human("Arina", "Bubenina", "Ivanovna", 7, _age = 18),
        Human("Valeria", "Popova", "Pavlovna", 4, _age = 18)
    )

    val simulationTime = 5
    println("Starting simulation for $simulationTime seconds...")


    for(i in 0..9) {
        println("\nmove ${i + 1}:")
        humans.forEach { it.move() }
    }

    println("\n final position ")
    humans.forEach {
        println("${it.name}: (${it.x}, ${it.y})")
    }
}

