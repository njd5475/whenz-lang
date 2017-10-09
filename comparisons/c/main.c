
#include <stdio.h>

int main(int argc, char** argv) {
  printf("Starting counting up");
  int counter = 0;
  while(counter <= 10000) {
    printf("Counter is less than 100:\n");
    printf("%d\n", counter);
    if(counter == 10000) {
      printf("Counter is equal to 100\n");
    }
    ++counter;
  }

  counter++;
  if(counter >= 10000) {
    printf("Counter is over 100\n");
  }
  return 0;
}
