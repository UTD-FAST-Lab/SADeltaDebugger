public class MainMultipleLineError5 {

    public static void main(String[] args){
        
        
        System.out.println(new Vehicle(0,4){
            @Override
            public int range(){
                return this.mpg/this.gas;
            }
        }.range());
    }

}
