#include <uv.h>
#include <stdio.h>
#include <stdlib.h>

static uv_buf_t buf;
static uv_tcp_t tcp;
static uv_write_t w;

int main(int argc, char **argv) {

  printf("Size of uv_buf_t = %lu\n", sizeof(buf));
  printf("Size of uv_tcp_t = %lu\n", sizeof(tcp));
  printf("Size of uv_write_t = %lu\n", sizeof(w));

}
