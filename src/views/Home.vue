<script setup>
// Sistema CTI Insights — Home institucional
// Fonts sugeridas (adicionar no index.html):
// Space Grotesk (títulos), Inter (corpo), JetBrains Mono (dados/labels)

const kpis = [
 { label: 'FATURAMENTO', value: 'R$ 842K', delta: '↑ 12,4%' },
 { label: 'CLIENTES NÍVEL A', value: '37', delta: '↑ 3 novos' },
]

const chartBars = [35, 52, 44, 68, 58, 80, 72, 95]

const problems = [
 {
 icon: '!',
 cardBg: 'bg-[#1a1d2b] border border-red-500/20',
 iconBg: 'bg-red-500/20 text-red-300',
 title: 'Dados fragmentados',
 text: 'Clientes, consultores e faturamento em arquivos separados, sem cruzamento entre si.',
 },
 {
 icon: '⚠',
 cardBg: 'bg-[#1d1b17] border border-amber-500/20',
 iconBg: 'bg-amber-500/20 text-amber-300',
 title: 'Decisão às cegas',
 text: 'Sem visão consolidada, quedas de faturamento em clientes-chave passam despercebidas.',
 },
 {
 icon: '✓',
 cardBg: 'bg-[#101d1a] border border-emerald-500/20',
 iconBg: 'bg-emerald-500/20 text-emerald-300',
 title: 'Solução: CTI Nexus',
 text: 'Upload da planilha, tratamento automático e dashboards prontos com KPIs estratégicos.',
 },
]

const steps = [
 { num: '01', title: 'Acesso seguro', text: 'Login restrito à equipe autorizada, com perfis por função.' },
 { num: '02', title: 'Upload da planilha', text: 'Envie o Excel operacional direto na plataforma.' },
 { num: '03', title: 'Tratamento automático', text: 'Validação, limpeza e estruturação dos dados sem esforço manual.' },
 { num: '04', title: 'Dashboards prontos', text: 'KPIs, rankings e alertas estratégicos, atualizados a cada envio.' },
]

const stats = [
 { value: '100%', label: 'Dados centralizados' },
 { value: 'A · B · C', label: 'Segmentação por nível' },
 { value: '  30s', label: 'Processamento por planilha' },
 { value: '01', label: 'Central única de decisão' },
]
</script>

<template>
 <div class="bg-[#050505] text-slate-100 font-sans">

 <!-- NAV -->
 <nav class="sticky top-0 z-50 bg-[#071026]/70 backdrop-blur-sm border-b border-white/6 shadow-sm">
 <div class="max-w-[1180px] mx-auto px-10 h-[76px] flex items-center justify-between">
 <div class="flex items-center gap-3">
 <svg viewBox="0 0 100 100" class="w-[30px] h-[30px]" fill="none" xmlns="http://www.w3.org/2000/svg">
 <defs>
 <linearGradient id="g1" x1="0" y1="100" x2="100" y2="0">
 <stop offset="0%" stop-color="#2563EB" />
 <stop offset="100%" stop-color="#10B981" />
 </linearGradient>
 </defs>
 <path d="M10 30 L10 85 L30 85 L30 55 L48 73 L48 55 L30 37 L30 30 Z" fill="white" opacity="0.95" />
 <path d="M48 55 L48 73 L66 85 L66 55 L48 37 Z" fill="white" opacity="0.95" />
 <path d="M8 48 L48 8 L70 30 L88 12 L88 38 L62 38 L74 26 L48 26 L28 46 Z" fill="url(#g1)" />
 </svg>
 <span class="font-display font-bold text-lg text-white">CTI Nexus</span>
 </div>
 <ul class="hidden md:flex gap-9">
 <li><a href="#problema" class="text-[14px] text-slate-300/80 hover:text-white transition-colors">Problema</a></li>
 <li><a href="#fluxo" class="text-[14px] text-slate-300/80 hover:text-white transition-colors">Como funciona</a></li>
 <li><a href="#resultados" class="text-[14px] text-slate-300/80 hover:text-white transition-colors">Resultados</a></li>
 </ul>
 <router-link to="/login" class="bg-gradient-to-r from-blue-600 to-blue-500 text-white font-semibold text-[13.5px] px-5 py-2.5 rounded-full shadow-md transform transition-all hover:scale-[1.02] inline-block">Entrar</router-link>
 </div>
 </nav>

 <!-- HERO -->
 <header class="relative overflow-hidden bg-gradient-to-br from-[#061226] via-[#071226]/60 to-[#070708] text-white py-24 md:py-[120px] border-b border-white/10">
 <div class="pointer-events-none absolute -top-[28%] -right-[4%] w-[720px] h-[720px] rounded-full bg-gradient-to-tr from-blue-700/30 via-blue-600/20 to-transparent blur-3xl"></div>
 <div class="pointer-events-none absolute -bottom-[40%] -left-[8%] w-[500px] h-[500px] rounded-full bg-emerald-500/8 blur-3xl"></div>
 <div class="pointer-events-none absolute -top-[8%] left-[10%] w-[220px] h-[220px] rounded-full bg-white/3 mix-blend-overlay blur-2xl"></div>
 <div class="pointer-events-none absolute inset-0 opacity-90" aria-hidden="true">
 <svg class="absolute inset-0 h-full w-full" viewBox="0 0 1600 700" preserveAspectRatio="none">
 <path d="M120 430 C 420 430, 440 430, 640 430 S 980 430, 1180 430 S 1430 430, 1480 430" stroke="rgba(125, 211, 252, 0.4)" stroke-width="2" fill="none"/>
 <path d="M200 300 L 520 300 L 640 450 L 980 450 L 1180 300 L 1420 300" stroke="rgba(96, 165, 250, 0.28)" stroke-width="2" fill="none"/>
 <path d="M200 520 L 520 520 L 640 290 L 980 290 L 1180 520 L 1420 520" stroke="rgba(52, 211, 153, 0.28)" stroke-width="2" fill="none"/>
 <circle cx="120" cy="430" r="5" fill="#7dd3fc" class="flow-dot dot-a"/>
 <circle cx="640" cy="430" r="5" fill="#7dd3fc" class="flow-dot dot-b"/>
 <circle cx="1180" cy="430" r="5" fill="#7dd3fc" class="flow-dot dot-c"/>
 <circle cx="1480" cy="430" r="5" fill="#7dd3fc" class="flow-dot dot-d"/>
 <circle cx="640" cy="450" r="5" fill="#34d399" class="flow-dot dot-e"/>
 <circle cx="1180" cy="300" r="5" fill="#34d399" class="flow-dot dot-f"/>
 </svg>
 </div>

 <div class="relative z-10 max-w-[1180px] mx-auto px-10 grid md:grid-cols-[1.1fr_0.9fr] gap-14 items-center">
 <div>
 <span class="inline-flex items-center gap-2 font-mono text-[11.5px] tracking-widest uppercase text-slate-300 bg-white/5 border border-white/10 px-3 py-1.5 rounded-full mb-6">
 <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Sistema CTI Insights
 </span>
 <h1 class="font-space font-bold text-6xl lg:text-11xl leading-tight tracking-tight text-white mb-6">
  Transforme dados em <span class="bg-gradient-to-r from-blue-400 to-emerald-400 bg-clip-text text-transparent">decisões.</span>
</h1>
 <p class="text-slate-300 text-base max-w-[460px] mb-9">
 Centralize clientes, consultores e faturamento espalhados em planilhas soltas — e converta isso em inteligência comercial acionável, em minutos.
 </p>
 <div class="flex items-center gap-4">
 <router-link to="/login" class="bg-gradient-to-r from-blue-600 to-blue-500 text-white font-semibold text-[14.5px] px-6 py-3.5 rounded-lg shadow-lg hover:from-blue-500 hover:to-blue-400 transition inline-block">Acessar plataforma</router-link>
 <a href="#fluxo" class="text-slate-200/90 font-medium text-[14.5px] inline-flex items-center gap-1.5">Ver como funciona →</a>
 </div>
 </div>

 <!-- Mock dashboard card -->
 <div class="bg-[#071829]/70 border border-white/10 rounded-[28px] p-6 md:p-7 backdrop-blur-md shadow-[0_30px_80px_rgba(15,118,110,0.18)] ring-1 ring-white/5">
 <div class="flex justify-between items-center mb-5">
 <span class="font-mono text-[11px] md:text-[12px] text-slate-300 uppercase tracking-[0.18em]">Visão Geral · Q3 2026</span>
 <span class="inline-flex items-center gap-2 font-mono text-[11px] md:text-[12px] text-slate-300">
 <span class="w-2 h-2 rounded-full bg-emerald-400 shadow-[0_0_10px_rgba(52,211,153,0.9)]"></span>
 ao vivo
 </span>
 </div>
 <div class="grid grid-cols-2 gap-4 mb-5">
 <div v-for="kpi in kpis" :key="kpi.label" class="bg-white/5 rounded-[14px] p-4 border border-white/6">
 <div class="font-mono text-[10px] md:text-[11px] text-slate-300 uppercase tracking-[0.12em]">{{ kpi.label }}</div>
 <div class="font-display text-[28px] md:text-[34px] font-bold text-white my-2 leading-none">{{ kpi.value }}</div>
 <div class="text-[11px] md:text-[12px] text-emerald-400 font-semibold">{{ kpi.delta }}</div>
 </div>
 </div>
 <div class="flex items-end gap-2 h-[110px] md:h-[132px] px-1 pt-2">
 <div
 v-for="(h, i) in chartBars"
 :key="i"
 class="flex-1 rounded-t-[8px] bg-gradient-to-b from-cyan-400 via-blue-500 to-emerald-400 opacity-95 shadow-[0_10px_18px_rgba(16,185,129,0.25)]"
 :style="{ height: h + '%' }"
 ></div>
 </div>
 </div>
 </div>
 </header>

 <!-- PROBLEMA -->
 <section id="problema" class="py-24 bg-transparent">
 <div class="max-w-[1180px] mx-auto px-10">
 <div class="font-mono text-[11.5px] tracking-widest uppercase text-blue mb-3">O problema</div>
 <h2 class="font-display font-bold text-3xl text-navy tracking-tight max-w-xl">
 Seus dados existem. Sua estratégia, ainda não.
 </h2>
 <p class="text-text-2 text-[15px] max-w-lg mt-3.5">
 Planilhas isoladas guardam informação valiosa — mas isolada, ela só serve pra consulta. Não gera padrão, não antecipa risco, não orienta decisão comercial.
 </p>

 <div class="grid md:grid-cols-3 gap-5 mt-12">
 <div v-for="p in problems" :key="p.title" :class="['rounded-xl p-7 text-slate-200 shadow-lg', p.cardBg]">
 <div :class="['w-9 h-9 rounded-lg flex items-center justify-center mb-4.5 text-base', p.iconBg]">{{ p.icon }}</div>
 <h3 class="font-display text-[16px] mb-2 text-white">{{ p.title }}</h3>
 <p class="text-[13.5px] text-slate-300">{{ p.text }}</p>
 </div>
 </div>
 </div>
 </section>

 <!-- FLUXO -->
 <section id="fluxo" class="relative overflow-hidden border-y border-white/8 py-24 bg-gradient-to-b from-[#020d1f] via-[#06182d] to-[#020812]">
 <div class="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(59,130,246,0.18),_transparent_35%),radial-gradient(circle_at_bottom_right,_rgba(16,185,129,0.12),_transparent_30%)]"></div>
 <div class="relative z-10 max-w-[1180px] mx-auto px-10">
 <div class="font-mono text-[11.5px] tracking-[0.25em] uppercase text-slate-300/90 mb-5">Como funciona</div>
 <h2 class="font-display font-bold text-4xl md:text-5xl text-white tracking-tight max-w-[760px] leading-[1.1]">
 Da planilha ao insight, em quatro passos.
 </h2>

 <div class="grid md:grid-cols-4 gap-8 lg:gap-12 mt-16 relative">
 <div class="hidden md:block absolute top-[18px] left-[8%] right-[8%] h-px bg-white/10"></div>
 <div v-for="s in steps" :key="s.num" class="relative z-10 pr-2">
 <div class="font-mono text-[14px] md:text-[16px] text-slate-200/80 mb-7 tracking-[0.06em]">
 {{ s.num }}
 </div>
 <h3 class="font-display text-[18px] md:text-[20px] mb-4 text-white leading-tight">{{ s.title }}</h3>
 <p class="text-[14px] md:text-[15px] leading-relaxed text-slate-300/85 max-w-[260px]">{{ s.text }}</p>
 </div>
 </div>
 </div>
 </section>

 <!-- RESULTADOS -->
 <section id="resultados" class="relative overflow-hidden bg-[#050505] text-white py-20 md:py-24 border-t border-white/10">
 <div class="absolute inset-0 z-0">
 <div class="absolute left-[8%] top-[58%] h-px w-[30%] bg-gradient-to-r from-transparent via-blue-400/40 to-blue-400/0"></div>
 <div class="absolute left-[35%] top-[38%] h-px w-[28%] bg-gradient-to-r from-transparent via-emerald-400/35 to-emerald-400/0"></div>
 <div class="absolute left-[62%] top-[58%] h-px w-[30%] bg-gradient-to-r from-transparent via-cyan-400/35 to-cyan-400/0"></div>
 <div class="absolute left-[18%] top-[58%] h-[120px] w-[120px] border-l border-t border-blue-400/20 rounded-tl-[40px] rotate-[-15deg]"></div>
 <div class="absolute right-[18%] top-[58%] h-[120px] w-[120px] border-r border-t border-cyan-400/20 rounded-tr-[40px] rotate-[15deg]"></div>
 <div class="absolute left-[32%] top-[28%] h-[120px] w-[120px] border-l border-b border-emerald-400/20 rounded-bl-[40px] rotate-[15deg]"></div>
 <div class="absolute right-[32%] top-[28%] h-[120px] w-[120px] border-r border-b border-emerald-400/20 rounded-br-[40px] rotate-[-15deg]"></div>
 <div class="absolute left-[18%] top-[58%] w-2.5 h-2.5 rounded-full bg-blue-400 shadow-[0_0_18px_rgba(96,165,250,1)] animate-pulse"></div>
 <div class="absolute left-[38%] top-[38%] w-2.5 h-2.5 rounded-full bg-emerald-400 shadow-[0_0_18px_rgba(52,211,153,1)] animate-pulse"></div>
 <div class="absolute left-[67%] top-[58%] w-2.5 h-2.5 rounded-full bg-cyan-400 shadow-[0_0_18px_rgba(34,211,238,1)] animate-pulse"></div>
 <div class="absolute right-[18%] top-[58%] w-2.5 h-2.5 rounded-full bg-cyan-400 shadow-[0_0_18px_rgba(34,211,238,1)] animate-pulse"></div>
 </div>

 <div class="relative z-10 max-w-[1180px] mx-auto px-10">
 <div class="font-mono text-[10px] md:text-[11px] tracking-[0.25em] uppercase text-slate-300 mb-6">Resultado esperado</div>
 <h2 class="font-display font-bold text-[2.2rem] md:text-[4rem] leading-[0.96] tracking-[-0.05em] text-white max-w-[980px] mb-12 md:mb-16">
 De planilha operacional a inteligência comercial.
 </h2>

 <div class="grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-10 lg:gap-14">
 <div v-for="s in stats" :key="s.label" class="min-h-[110px] flex flex-col justify-end">
 <div class="font-display font-bold text-[2.4rem] md:text-[4rem] leading-none tracking-[-0.06em] text-white">{{ s.value }}</div>
 <div class="font-mono text-[9px] md:text-[10px] text-slate-300 mt-2 uppercase tracking-[0.14em] leading-relaxed">{{ s.label }}</div>
 </div>
 </div>
 </div>
 </section>

 <!-- CTA FINAL -->
 <section id="login" class="relative overflow-hidden bg-[#050505] py-28 border-t border-white/10">
 <div class="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/20 to-transparent"></div>
 <div class="absolute inset-0 bg-[radial-gradient(circle_at_center,_rgba(59,130,246,0.18),_transparent_35%)]"></div>
 <div class="absolute left-1/2 top-1/2 h-[260px] w-[260px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-blue-500/5 blur-3xl"></div>
 <div class="relative z-10 max-w-[1180px] mx-auto px-10 text-center">
 <div class="font-mono text-[11px] md:text-[12px] tracking-[0.25em] uppercase text-slate-300 mb-8">Pronto para começar</div>
 <h2 class="font-display font-bold text-[2.6rem] md:text-[4rem] leading-[0.96] tracking-[-0.05em] text-white mx-auto mb-6 max-w-[980px]">
 Acesse a plataforma e veja seus dados com outros olhos.
 </h2>
 <p class="text-slate-300 text-[15px] md:text-[18px] mx-auto mb-10">Área restrita a funcionários autorizados da CTI.</p>
 <router-link to="/login" class="inline-flex items-center justify-center gap-3 bg-gradient-to-r from-blue-600 to-blue-500 text-white font-semibold text-[14px] md:text-[16px] px-8 py-4 rounded-xl shadow-[0_18px_40px_rgba(37,99,235,0.45)] hover:translate-y-[-1px] transition-transform">Fazer login <span aria-hidden="true">→</span></router-link>
 </div>
 </section>

 <!-- FOOTER -->
 <footer class="bg-black text-slate-300 py-10 border-t border-white/10">
 <div class="max-w-[1180px] mx-auto px-10 flex justify-between text-[12.5px]">
 <span class="font-mono text-white">CTI NEXUS</span>
 <span class="font-mono">SISTEMA CTI INSIGHTS © 2026</span>
 </div>
 </footer>

 </div>
</template>

<style scoped>
.flow-dot {
  filter: drop-shadow(0 0 10px rgba(125, 211, 252, 0.9));
  animation: pulse 2.2s ease-in-out infinite;
}

.dot-a { animation-delay: 0s; }
.dot-b { animation-delay: 0.45s; }
.dot-c { animation-delay: 0.9s; }
.dot-d { animation-delay: 1.35s; }
.dot-e { animation-delay: 1.8s; }
.dot-f { animation-delay: 2.2s; }

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
    transform: scale(0.9);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}
</style>

<style scoped>
.flow-dot {
  filter: drop-shadow(0 0 10px rgba(125, 211, 252, 0.9));
  animation: pulse 2.2s ease-in-out infinite;
}

.dot-a { animation-delay: 0s; }
.dot-b { animation-delay: 0.45s; }
.dot-c { animation-delay: 0.9s; }
.dot-d { animation-delay: 1.35s; }
.dot-e { animation-delay: 1.8s; }
.dot-f { animation-delay: 2.2s; }

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
    transform: scale(0.9);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}
</style>