declare module '*.vue' {
	import type { DefineComponent } from 'vue'

	const component: DefineComponent<Record<string, never>, Record<string, never>, any>
	export default component
}

declare module '*.wxs' {
	const content: any
	export default content
}

declare const uni: any
declare const process: {
	env?: Record<string, string | undefined>
}
