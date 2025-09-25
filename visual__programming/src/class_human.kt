import kotlin.random.Random

open class Human : Moviable {
    public var name: String = ""
    public var surname: String = ""
    public var second_name: String = ""
    override var x: Int = 0
    override var y: Int = 0
    override var current_speed: Int = 7
        set(value) {
            if (value in 0..16){
                field = value
            } else {
                println("ошибка, скорость $value невозможна для человека")
            }
        }
        get() {
            return field
        }
   public var age: Int = 0
        set(value) {
            if (value in 0..100) {
                field = value
            } else {
                println("введенный возраст $value некорректен")
            }
        }
       get() {
           return field
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