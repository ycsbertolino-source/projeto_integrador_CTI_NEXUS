import { defineStore } from 'pinia'

export const useTarefasStore = defineStore('tarefas', {
//STATE = os dados que serão armazenados e compartilhados entre os componentes
    state: () => ({
    lista: [], //começa vazia
    }),

    //GETTERS = uma conta feita em cima dos dados 
    getters:{

        total: (state) => state.lista.length
    },
    //ACTIONS=funçoes para mudar os dados 
    actions: {
        adcionar(texto){
            this.lista.push(texto) 

            },
            //Remover um item da lista
            remover(indice){
                this.lista.splice(indice, 1)
            }
        }
    })

