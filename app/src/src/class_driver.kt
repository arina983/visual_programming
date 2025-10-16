import kotlin.random.Random

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