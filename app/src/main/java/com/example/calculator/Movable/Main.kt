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

