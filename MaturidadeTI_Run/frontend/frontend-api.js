/**
 * FRONTEND-API.js - Integração Frontend com Backend
 * MaturidadeTI v2.0 - API Client
 */

const LOCAL_API_URL = 'http://localhost:8080/api/v1';
const PROD_API_URL = 'https://maturidade-ti-backend.fly.dev/api/v1';

function normalizeApiUrl(url) {
  return url.replace(/\/+$/, '');
}

function resolveApiBaseUrl() {
  if (typeof window !== 'undefined') {
    const overrideUrl = window.__MATURITY_API_BASE_URL__;
    if (typeof overrideUrl === 'string' && overrideUrl.trim()) {
      return normalizeApiUrl(overrideUrl.trim());
    }

    const isLocalHost =
      window.location.protocol === 'file:' ||
      window.location.hostname === '' ||
      window.location.hostname === 'localhost' ||
      window.location.hostname === '127.0.0.1';

    if (isLocalHost) {
      return LOCAL_API_URL;
    }
  }

  return PROD_API_URL;
}

const API_CONFIG = {
  BASE_URL: resolveApiBaseUrl(),
  TOKEN_KEY: 'maturidade_ti_token',
  USER_KEY: 'maturidade_ti_user'
};

const PERFIL_LABEL_TO_API = {
  'ADMINISTRADOR': 'ADMIN',
  'AVALIADOR': 'AVALIADOR',
  'GESTOR': 'GESTOR',
  'CLIENTE': 'CLIENTE'
};

// ═══════════════════════════════════════════════════════════════
// UTILITÁRIOS DE REQUISIÇÃO
// ═══════════════════════════════════════════════════════════════

function getAuthHeaders() {
  const token = localStorage.getItem(API_CONFIG.TOKEN_KEY);
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` })
  };
}

async function apiCall(endpoint, method = 'GET', data = null) {
  try {
    const options = {
      method,
      headers: getAuthHeaders()
    };

    if (data) {
      options.body = JSON.stringify(data);
    }

    const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, options);

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem(API_CONFIG.TOKEN_KEY);
        localStorage.removeItem(API_CONFIG.USER_KEY);
        nav('login');
        throw new Error('Sessão expirada. Faça login novamente.');
      }
      let errorMessage = `Erro ${response.status}`;
      const responseText = await response.text();

      if (responseText) {
        try {
          const errorJson = JSON.parse(responseText);
          errorMessage = errorJson.message || errorJson.error || errorMessage;
        } catch {
          errorMessage = responseText;
        }
      }

      throw new Error(errorMessage);
    }

    return await response.json();
  } catch (error) {
    console.error('Erro na requisição:', error);
    throw error;
  }
}

function mostrarErro(msg, elementId = null) {
  console.error(msg);
  if (elementId) {
    const el = document.getElementById(elementId);
    if (el) {
      el.textContent = msg;
      el.style.display = 'block';
      setTimeout(() => { el.style.display = 'none'; }, 5000);
    }
  }
}

function mostrarSucesso(msg, elementId = null) {
  console.log(msg);
  if (elementId) {
    const el = document.getElementById(elementId);
    if (el) {
      el.style.display = 'block';
      setTimeout(() => { el.style.display = 'none'; }, 3000);
    }
  }
}

// ═══════════════════════════════════════════════════════════════
// AUTENTICAÇÃO
// ═══════════════════════════════════════════════════════════════

async function fazerLoginAPI() {
  const email = document.getElementById('login-email').value.trim();
  const senha = document.getElementById('login-senha').value;
  const err = document.getElementById('login-err');
  const ok = document.getElementById('login-ok');
  const box = document.getElementById('token-box');

  err.style.display = 'none';
  ok.style.display = 'none';
  box.style.display = 'none';

  if (!email || !senha) {
    mostrarErro('Preencha e-mail e senha.', 'login-err');
    return;
  }

  try {
    const response = await apiCall('/auth/login', 'POST', { email, senha });
    
    localStorage.setItem(API_CONFIG.TOKEN_KEY, response.token);
    localStorage.setItem(API_CONFIG.USER_KEY, JSON.stringify(response));

    document.getElementById('token-val').textContent = response.token;
    ok.style.display = 'block';
    box.style.display = 'block';

    setTimeout(() => {
      entrar();
      nav('dashboard');
      carregarDashboard();
    }, 1500);
  } catch (error) {
    mostrarErro(error.message, 'login-err');
  }
}

async function fazerCadastroAPI() {
  const nome = document.getElementById('cad-nome').value.trim();
  const sobrenome = document.getElementById('cad-sobre').value.trim();
  const email = document.getElementById('cad-email').value.trim();
  const senha = document.getElementById('cad-senha').value;
  const perfilSelecionado = document.querySelector('.perfil.on .perfil-name')?.textContent?.trim()?.toUpperCase();
  const perfil = PERFIL_LABEL_TO_API[perfilSelecionado];
  
  const err = document.getElementById('cad-err');
  const ok = document.getElementById('cad-ok');

  err.style.display = 'none';
  ok.style.display = 'none';

  if (!nome || !sobrenome || !email || !senha || !perfil) {
    mostrarErro('Preencha todos os campos.', 'cad-err');
    return;
  }

  try {
    const response = await apiCall('/auth/cadastro', 'POST', {
      nome, sobrenome, email, senha, perfil
    });

    localStorage.setItem(API_CONFIG.TOKEN_KEY, response.token);
    localStorage.setItem(API_CONFIG.USER_KEY, JSON.stringify(response));

    ok.style.display = 'block';
    setTimeout(() => {
      entrar();
      nav('dashboard');
      carregarDashboard();
    }, 1500);
  } catch (error) {
    mostrarErro(error.message, 'cad-err');
  }
}

function logout() {
  localStorage.removeItem(API_CONFIG.TOKEN_KEY);
  localStorage.removeItem(API_CONFIG.USER_KEY);
  localStorage.removeItem('aval_id');
  localStorage.removeItem('relatorio');
  nav('login');
}

function temPermissao(role) {
  const user = JSON.parse(localStorage.getItem(API_CONFIG.USER_KEY) || '{}');
  const rolesPermitidas = {
    'admin': ['ADMIN'],
    'criar_empresa': ['ADMIN'],
    'avaliar': ['ADMIN', 'AVALIADOR'],
    'ver_relatorio': ['ADMIN', 'GESTOR']
  };
  return rolesPermitidas[role]?.includes(user.role);
}

// ═══════════════════════════════════════════════════════════════
// EMPRESAS
// ═══════════════════════════════════════════════════════════════

async function carregarEmpresas() {
  try {
    const empresas = await apiCall('/empresas/ativas');
    const tbody = document.getElementById('emp-tbody');
    const sel = document.getElementById('sel-empresa');

    tbody.innerHTML = '';
    sel.innerHTML = '<option value="">Selecione a empresa...</option>';

    empresas.forEach(emp => {
      // Tabela
      const tr = document.createElement('tr');
      tr.setAttribute('data-nome', emp.razaoSocial);
      tr.style = 'border-bottom:.5px solid var(--border)';
      tr.innerHTML = `
        <td style="padding:10px"><span class="avatar">${iniciais(emp.razaoSocial)}</span>${emp.razaoSocial}</td>
        <td style="padding:10px;color:var(--muted)">${emp.setor}</td>
        <td style="padding:10px;color:var(--muted)">${emp.responsavel}</td>
        <td style="padding:10px"><span class="badge ${badgeCls(emp.status)}">${emp.status}</span></td>
        <td style="padding:10px"><button class="btn-danger" onclick="removerEmpAPI(${emp.id})">Remover</button></td>
      `;
      tbody.appendChild(tr);

      // Select para avaliação
      const opt = document.createElement('option');
      opt.value = emp.id;
      opt.textContent = emp.razaoSocial;
      sel.appendChild(opt);
    });

    atualizarEmpCount();
    document.getElementById('emp-empty').style.display = empresas.length === 0 ? 'block' : 'none';
  } catch (error) {
    mostrarErro('Erro ao carregar empresas: ' + error.message);
  }
}

async function cadEmpresaAPI() {
  const razaoSocial = document.getElementById('emp-razao').value.trim();
  const cnpj = document.getElementById('emp-cnpj').value.trim();
  const setor = document.getElementById('emp-setor').value;
  const porte = document.getElementById('emp-porte').value;
  const responsavel = document.getElementById('emp-contato').value.trim();
  const email = document.getElementById('emp-email').value.trim();
  const telefone = document.getElementById('emp-tel').value.trim();
  
  const err = document.getElementById('emp-err');
  const ok = document.getElementById('emp-ok');

  err.style.display = 'none';
  ok.style.display = 'none';

  if (!razaoSocial || !cnpj || !setor || !responsavel || !email) {
    mostrarErro('Preencha os campos obrigatórios.', 'emp-err');
    return;
  }

  try {
    await apiCall('/empresas', 'POST', {
      razaoSocial, cnpj, setor, porte, responsavel, email, telefone
    });

    mostrarSucesso('Empresa cadastrada com sucesso!', 'emp-ok');
    
    // Limpar formulário
    ['emp-razao', 'emp-cnpj', 'emp-contato', 'emp-email', 'emp-tel', 'emp-obs'].forEach(
      id => document.getElementById(id).value = ''
    );
    document.getElementById('emp-setor').value = '';
    document.getElementById('emp-porte').value = '';
    document.getElementById('emp-status').value = 'Ativo';

    // Recarregar lista
    await carregarEmpresas();
  } catch (error) {
    mostrarErro(error.message, 'emp-err');
  }
}

async function removerEmpAPI(id) {
  if (!confirm('Tem certeza que deseja remover esta empresa?')) return;

  try {
    await apiCall(`/empresas/${id}`, 'DELETE');
    await carregarEmpresas();
  } catch (error) {
    mostrarErro('Erro ao remover empresa: ' + error.message);
  }
}

// ═══════════════════════════════════════════════════════════════
// QUESTÕES
// ═══════════════════════════════════════════════════════════════

async function carregarQuestoes() {
  try {
    const questoesData = await apiCall('/questoes/listar');
    const qList = document.getElementById('q-list');

    qList.innerHTML = '';

    questoesData.forEach(q => {
      const a = areaMap[q.area] || { cls: 'b-inf', l: q.area };
      const div = document.createElement('div');
      div.className = 'q-card';
      div.innerHTML = `
        <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:10px;margin-bottom:6px">
          <div style="font-size:13px;color:var(--ink);line-height:1.5;flex:1">${q.enunciado}</div>
          <button class="btn-danger" onclick="removerQuestaoAPI(${q.id})">✕</button>
        </div>
        <div class="q-meta">
          <span class="badge ${a.cls}">${a.l}</span>
          <span class="framework">${q.framework || 'Sem framework'}</span>
        </div>
        ${q.evidenciaEsperada ? `<div style="margin-top:5px;font-size:11px;color:var(--muted)">Evidência: ${q.evidenciaEsperada}</div>` : ''}
      `;
      qList.appendChild(div);
    });

    document.getElementById('q-count').textContent = questoesData.length;
    document.getElementById('q-empty').style.display = questoesData.length === 0 ? 'block' : 'none';
  } catch (error) {
    mostrarErro('Erro ao carregar questões: ' + error.message);
  }
}

async function addQuestaoAPI() {
  const area = document.getElementById('q-area').value;
  const enunciado = document.getElementById('q-texto').value.trim();
  const framework = document.getElementById('q-fw').value;
  const criticidade = document.getElementById('q-nivel').value;
  const evidenciaEsperada = document.getElementById('q-ev').value.trim();

  const err = document.getElementById('q-err');
  const ok = document.getElementById('q-ok');

  err.style.display = 'none';
  ok.style.display = 'none';

  if (!area || !enunciado || !criticidade) {
    mostrarErro('Preencha os campos obrigatórios.', 'q-err');
    return;
  }

  try {
    await apiCall('/questoes', 'POST', {
      area, enunciado, framework, criticidade, evidenciaEsperada
    });

    mostrarSucesso('Questão adicionada!', 'q-ok');

    // Limpar formulário
    ['q-area', 'q-texto', 'q-fw', 'q-nivel', 'q-ev'].forEach(
      id => document.getElementById(id).value = ''
    );

    // Recarregar lista
    await carregarQuestoes();
  } catch (error) {
    mostrarErro(error.message, 'q-err');
  }
}

async function removerQuestaoAPI(id) {
  try {
    await apiCall(`/questoes/${id}`, 'DELETE');
    await carregarQuestoes();
  } catch (error) {
    mostrarErro('Erro ao remover questão: ' + error.message);
  }
}

// ═══════════════════════════════════════════════════════════════
// AVALIAÇÕES
// ═══════════════════════════════════════════════════════════════

let avaliacaoAtual = null;
let respostasTemp = {};

async function iniciarAvalAPI() {
  const sel = document.getElementById('sel-empresa');
  if (!sel.value) {
    sel.style.borderColor = '#E24B4A';
    setTimeout(() => { sel.style.borderColor = ''; }, 1500);
    return;
  }

  try {
    const avaliacao = await apiCall(`/avaliacoes/iniciar?empresaId=${sel.value}`, 'POST');
    avaliacaoAtual = avaliacao;
    respostasTemp = {};

    localStorage.setItem('aval_id', avaliacao.id);

    document.getElementById('prog-wrap').style.display = 'block';
    document.getElementById('prog-emp').textContent = sel.options[sel.selectedIndex].text;
    document.getElementById('area-tabs').style.display = 'flex';
    document.getElementById('aval-empty').style.display = 'none';
    document.getElementById('finalizar-row').style.display = 'flex';

    // Carregar questões
    await carregarQuestoesAval();
  } catch (error) {
    mostrarErro('Erro ao iniciar avaliação: ' + error.message);
  }
}

async function carregarQuestoesAval() {
  try {
    const questoes = await apiCall('/questoes/listar');
    renderAreaTabsAval(questoes);
    renderQAvalAPI(questoes, 'todos');
  } catch (error) {
    mostrarErro('Erro ao carregar questões: ' + error.message);
  }
}

function renderAreaTabsAval(questoes) {
  const tabs = document.getElementById('area-tabs');
  tabs.innerHTML = '';

  const areas = ['todos', ...new Set(questoes.map(q => q.area))];
  areas.forEach(area => {
    const btn = document.createElement('button');
    btn.className = 'area-tab' + (area === 'todos' ? ' active' : '');

    const count = area === 'todos' ? questoes.length : questoes.filter(q => q.area === area).length;
    const areaLabel = area === 'todos' ? 'Todas' : (areaMap[area]?.l || area);

    btn.innerHTML = `${areaLabel} <span style="font-size:10px;margin-left:4px;opacity:.7">(${count})</span>`;
    btn.onclick = () => {
      document.querySelectorAll('.area-tab').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderQAvalAPI(questoes, area);
    };

    tabs.appendChild(btn);
  });
}

function renderQAvalAPI(questoes, area) {
  const wrap = document.getElementById('q-wrap-aval');
  wrap.innerHTML = '';

  const lista = area === 'todos' ? questoes : questoes.filter(q => q.area === area);

  lista.forEach((q, i) => {
    const resp = respostasTemp[q.id];
    let cls = 'q-item';
    if (resp === 'OK') cls += ' r-ok';
    else if (resp === 'PARCIAL') cls += ' r-parc';
    else if (resp === 'NAO_OK') cls += ' r-nao';

    const mostra = resp === 'PARCIAL' || resp === 'NAO_OK';
    const a = areaMap[q.area] || { cls: 'b-inf', l: q.area };

    const div = document.createElement('div');
    div.className = cls;
    div.id = 'qi-' + q.id;

    div.innerHTML = `
      <div class="q-num">Questão ${i + 1} de ${lista.length}</div>
      <div class="q-text">${q.enunciado}</div>
      <div class="q-meta" style="margin-bottom:10px">
        <span class="badge ${a.cls}">${a.l}</span>
        <span class="framework">${q.framework || ''}</span>
      </div>
      <div class="resp-opts">
        <button class="resp-btn ok-b${resp === 'OK' ? ' sel' : ''}" onclick="responderAvalAPI(${q.id}, 'OK', this)">✔ OK</button>
        <button class="resp-btn parc-b${resp === 'PARCIAL' ? ' sel' : ''}" onclick="responderAvalAPI(${q.id}, 'PARCIAL', this)">◑ Parcial</button>
        <button class="resp-btn nao-b${resp === 'NAO_OK' ? ' sel' : ''}" onclick="responderAvalAPI(${q.id}, 'NAO_OK', this)">✘ Não OK</button>
      </div>
      ${q.evidenciaEsperada ? `<div class="obs-hint" id="hint-${q.id}" style="display:${mostra ? 'block' : 'none'}">Evidência esperada: <em>${q.evidenciaEsperada}</em></div>` : ''}
      <textarea class="obs-field" id="obs-${q.id}" placeholder="Descreva a situação ou plano de ação..." style="display:${mostra ? 'block' : 'none'}"></textarea>
    `;

    wrap.appendChild(div);
  });
}

async function responderAvalAPI(questaoId, resposta, btn) {
  const item = document.getElementById('qi-' + questaoId);
  const obsField = document.getElementById('obs-' + questaoId);
  const hintField = document.getElementById('hint-' + questaoId);

  respostasTemp[questaoId] = resposta;

  item.className = 'q-item r-' + (resposta === 'OK' ? 'ok' : resposta === 'PARCIAL' ? 'parc' : 'nao');
  item.querySelectorAll('.resp-btn').forEach(b => b.classList.remove('sel'));
  btn.classList.add('sel');

  const mostra = resposta === 'PARCIAL' || resposta === 'NAO_OK';
  if (obsField) obsField.style.display = mostra ? 'block' : 'none';
  if (hintField) hintField.style.display = mostra ? 'block' : 'none';

  // Registrar no backend
  try {
    const observacao = obsField?.value || '';
    await apiCall(`/avaliacoes/${avaliacaoAtual.id}/responder`, 'POST', {
      questaoId, resposta, observacao
    });
  } catch (error) {
    mostrarErro('Erro ao registrar resposta: ' + error.message);
  }

  atualizarProgAPI();
}

function atualizarProgAPI() {
  const respostas = Object.values(respostasTemp);
  const total = Object.keys(respostasTemp).length;
  const ok = respostas.filter(r => r === 'OK').length;
  const parc = respostas.filter(r => r === 'PARCIAL').length;
  const nao = respostas.filter(r => r === 'NAO_OK').length;
  const pend = total - ok - parc - nao;
  const pct = total > 0 ? Math.round(((ok + parc + nao) / total) * 100) : 0;

  document.getElementById('prog-fill').style.width = pct + '%';
  document.getElementById('prog-pct').textContent = pct + '%';
  document.getElementById('cnt-ok').textContent = ok;
  document.getElementById('cnt-parc').textContent = parc;
  document.getElementById('cnt-nao').textContent = nao;
  document.getElementById('cnt-pend').textContent = pend;

  const btn = document.getElementById('btn-fin');
  const hint = document.getElementById('fin-hint');

  if (pend === 0 && total > 0) {
    btn.disabled = false;
    hint.textContent = 'Todas respondidas!';
    hint.style.color = '#0F6E56';
  } else {
    btn.disabled = true;
    hint.textContent = pend > 0 ? `${pend} pendente(s)` : 'Responda as questões';
    hint.style.color = 'var(--muted)';
  }
}

async function finalizarAvalAPI() {
  const observacoes = document.querySelector('[placeholder="Observações gerais da avaliação"]')?.value || '';

  try {
    const avaliacao = await apiCall(`/avaliacoes/${avaliacaoAtual.id}/finalizar`, 'POST', {
      observacoes
    });

    localStorage.setItem('relatorio', JSON.stringify(avaliacao));
    exibirRelatorioAPI(avaliacao);
    nav('relatorio');
  } catch (error) {
    mostrarErro('Erro ao finalizar avaliação: ' + error.message);
  }
}

function exibirRelatorioAPI(avaliacao) {
  const { scoreTotal: score, nivel, respostas } = avaliacao;

  document.getElementById('rel-score').textContent = Math.round(score || 0);
  document.getElementById('rel-emp').textContent = avaliacao.empresaNome;
  document.getElementById('rel-meta').textContent = `Avaliação realizada em ${new Date().toLocaleDateString('pt-BR')} · Avaliador: ${avaliacao.avaliadorNome}`;

  const nivelMap = { 'ARTESANAL': 'Artesanal', 'EFETIVA': 'Efetiva', 'EFICIENTE': 'Eficiente', 'ESTRATEGICA': 'Estratégica' };
  document.getElementById('rel-nivel-badge').textContent = `Nível: ${nivelMap[nivel] || 'Indefinido'}`;

  const ok = respostas.filter(r => r.resposta === 'OK').length;
  const parc = respostas.filter(r => r.resposta === 'PARCIAL').length;
  const nao = respostas.filter(r => r.resposta === 'NAO_OK').length;

  document.getElementById('rel-ok').textContent = ok;
  document.getElementById('rel-parc').textContent = parc;
  document.getElementById('rel-nao').textContent = nao;
}

// ═══════════════════════════════════════════════════════════════
// DASHBOARD
// ═══════════════════════════════════════════════════════════════

async function carregarDashboard() {
  try {
    // Carregar dados do dashboard
    await carregarEmpresas();
    await carregarQuestoes();
  } catch (error) {
    console.error('Erro ao carregar dashboard:', error);
  }
}

// ═══════════════════════════════════════════════════════════════
// INICIALIZAÇÃO
// ═══════════════════════════════════════════════════════════════

// Verificar se usuário já está logado
window.addEventListener('load', () => {
  const token = localStorage.getItem(API_CONFIG.TOKEN_KEY);
  if (token) {
    entrar();
    carregarDashboard();
  }

  // Sobrescrever funções originais
  window.fazerLogin = fazerLoginAPI;
  window.fazerCadastro = fazerCadastroAPI;
  window.cadEmpresa = cadEmpresaAPI;
  window.addQuestao = addQuestaoAPI;
  window.iniciarAval = iniciarAvalAPI;
  window.finalizarAval = finalizarAvalAPI;
});
