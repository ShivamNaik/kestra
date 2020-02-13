<template>
    <div v-if="flow">
        <b-row>
            <b-col md="1">
                <p>Display type</p>
            </b-col>
            <b-col md="2">
                <b-button-group>
                    <b-button @click="isTab = false">Single page</b-button>
                    <b-button @click="isTab = true">Tabs</b-button>
                </b-button-group>
            </b-col>
        </b-row>
        <hr />
        <div v-if="isTab">
            <b-tabs content-class="mt-3">
                <b-tab title="revision A">
                    <b-form-select v-model="selectedA" :options="options"></b-form-select>
                    <pre>{{a}}</pre>
                </b-tab>
                <b-tab title="Diff" active>
                    <pre class="diff"><div v-html="diffHtml" /></pre>
                </b-tab>
                <b-tab title="revision B">
                    <b-form-select v-model="selectedB" :options="options"></b-form-select>
                    <pre>{{b}}</pre>
                </b-tab>
            </b-tabs>
        </div>
        <b-row v-else>
            <b-col md="4">
                <b-form-select v-model="selectedA" :options="options"></b-form-select>
                <pre>{{a}}</pre>
            </b-col>
            <b-col md="4">
                <p>Diff A - B</p>
                <pre class="diff"><div v-html="diffHtml" /></pre>
            </b-col>
            <b-col md="4">
                <b-form-select v-model="selectedB" :options="options"></b-form-select>
                <p>Revision B</p>
                <pre>{{b}}</pre>
            </b-col>
        </b-row>
    </div>
</template>
<script>
import RouteContext from "../../mixins/routeContext";
import { mapState } from "vuex";
import Yaml from "yaml";
var Diff = require("text-diff");
export default {
    mixins: [RouteContext],
    components: {},
    created() {
        this.$store.dispatch("flow/loadFlow", this.$route.params).then(() => {
            this.selectedA = this.options[0]
            this.selectedB = this.options[this.options.length - 1]
            console.log(this.options, this.selectedA, this.selectedB)

        }) ;
    },
    data() {
        return {
            isTab: false,
            selectedA: undefined,
            selectedB: undefined
        };
    },
    methods: {},
    computed: {
        ...mapState("flow", ["flow"]),
        options() {
            const options = [];
            for (let i = 0; i < 5; i++) {
                options.push({ value: i, text: `revision ${i}` });
            }
            return options;
        },
        a() {
            return Yaml.stringify(this.flow) || "";
        },
        b() {
            const f = JSON.parse(JSON.stringify(this.flow));
            f.tasks[0].cases.SECOND = null;
            f.tasks[0].cases.FLAF = {
                test: 1
            };
            return Yaml.stringify(f, null, 4) || "";
        },
        diffHtml() {
            var diff = new Diff(); // options may be passed to constructor; see below
            var textDiff = diff.main(this.a, this.b); // produces diff array
            return diff.prettyHtml(textDiff); // produces a formatted HTML string
        },
        routeInfo() {
            return {
                title: this.$route.params.id,
                breadcrumb: [
                    {
                        label: this.$t("flows"),
                        link: {
                            name: "flowsList"
                        }
                    },
                    {
                        label: this.$route.params.namespace,
                        link: {
                            name: "flowsList",
                            query: {
                                namespace: this.$route.params.namespace
                            }
                        }
                    },
                    {
                        label: this.$route.params.id,
                        link: {
                            name: "flow",
                            params: {
                                namespace: this.$route.params.namespace,
                                id: this.$route.params.id
                            }
                        }
                    }
                ]
            };
        }
    }
};
</script>
<style scoped>
.diff >>> ins {
    background-color: #8bca4f;
    color: white;
    text-decoration: none;
}
.diff >>> del {
    background-color: #ca8b4f;
    color: white;
    text-decoration: none;
}
</style>