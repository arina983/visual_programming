import kotlin.random.Random

interface Moviable{
    var x: Int
    var y: Int
    var current_speed : Int
    fun move(): Thread
}

open class Human : Moviable {
    public var name: String = ""
        get() = field
        set(value) {
            field = value
        }
   public var surname: String = ""
        get() = field
        set(value) {
            field = value
        }
   public var second_name: String = ""
        get() = field
        set(value) {
            field = value
        }
   override var x: Int = 0
        get() {
            return field
        }
        set(value) {
            field = value
        }
   override var y: Int = 0
        get() {
            return field
        }
        set(value) {
            field = value
        }
    override var current_speed: Int = 7
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
   public var age: Int = 0
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

   public constructor(_name: String, _surname: String, _second: String, _cs: Int, _age: Int){
        name = _name
        surname= _surname
        second_name = _second
        current_speed = _cs
        age = _age
        println("We created the human object with name and age: $surname $name $second_name, age: $age")
    }

    override open fun move(): Thread{
       val thread = Thread {
           val dx = Random.nextInt(-current_speed, current_speed + 1)
           val dy = Random.nextInt(-current_speed, current_speed + 1)

           x += dx
           y += dy

           println("$name (speed: $current_speed) moved by: ($dx, $dy) to: ($x, $y)")
       }
       thread.start()
       return thread
   }
}

class Driver: Human {
    constructor(_name: String, _surname: String, _second: String, _cs: Int, _age: Int) :
    super(_name, _surname, _second, _cs, _age)

    override fun move(): Thread{
        val thread = Thread {
            val dx = Random.nextInt(-current_speed, current_speed + 1)
            x += dx
            println("$name (Driver, speed: $current_speed) moved linearly by: ($dx) to: ($x)")
        }
        thread.start()
        return thread
    }
}

fun main(){

    val humans = arrayOf(
        Human("Arina", "Bubenina", "Ivanovna", 7, _age = 18),
        Human("Valeria", "Popova", "Pavlovna", 4, _age = 18)
    )
    val driver = arrayOf(
        Driver(_name = "Sonya", _surname = "Pac", _second = "Sergeevna", _cs = 10, _age = 21)
    )

    val simulationTime = 5
    println("Starting simulation for $simulationTime seconds...")


    for(i in 0 ..simulationTime) {
        println("\nmove ${i + 1}:")

        val threads = mutableListOf<Thread>()
        humans.forEach { threads.add(it.move()) }
        driver.forEach { threads.add(it.move()) }
        threads.forEach { it.join() }

    }

    println("\n final position ")
    humans.forEach {
        println("${it.name}: (${it.x}, ${it.y})")
    }
    driver.forEach { println("${it.name}(Driver): (${it.x})") }
}

