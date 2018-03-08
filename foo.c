#include <uv.h>
#include <stdio.h>
#include <stdlib.h>

static uv_write_t w;

int main(int argc, char **argv) {

  printf("Size of uv_write_t = %lu\n", sizeof(w));

}
