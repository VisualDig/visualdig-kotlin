import Elm from './elm';

export default function ElmApp(portBindings) {
    this.elm = Elm.Main.fullscreen()
}