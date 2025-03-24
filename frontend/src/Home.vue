<!-- 轮播图 -->
<section class="mb-10">
  <el-carousel height="400px" indicator-position="outside">
    <el-carousel-item v-for="(banner, index) in banners" :key="index">
      <div class="w-full h-full bg-cover bg-center rounded-lg overflow-hidden" :style="{ backgroundImage: `url(${banner.image})` }">
        <div class="w-full h-full flex items-center bg-black bg-opacity-30 px-10">
          <div class="text-white max-w-xl">
            <h2 class="text-3xl font-bold mb-4">{{ banner.title }}</h2>
            <p class="text-lg mb-6">{{ banner.description }}</p>
            <router-link v-if="banner.link" :to="banner.link" class="btn">
              了解更多
            </router-link>
          </div>
        </div>
      </div>
    </el-carousel-item>
  </el-carousel>
</section>

// 轮播图数据
const banners = ref([
  {
    title: '2023年全国大学生运动会',
    description: '展示青春活力，挑战自我极限！',
    image: 'https://picsum.photos/1200/400?random=1',
    link: '/events/1'
  },
  {
    title: '篮球联赛火热报名中',
    description: '组建你的梦之队，争夺最高荣誉！',
    image: 'https://picsum.photos/1200/400?random=2',
    link: '/events/2'
  },
  {
    title: '校园马拉松即将开跑',
    description: '突破自我，跑出你的精彩人生！',
    image: 'https://picsum.photos/1200/400?random=3',
    link: '/events/3'
  }
]) 